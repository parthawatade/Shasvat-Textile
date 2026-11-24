package com.example.data.network

import com.example.BuildConfig
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class CreateGatewayOrderRequest(
    val orderNumber: String,
    val amountPaise: Int,
    val currency: String = "INR",
    val paymentMethod: String,
    val customerEmail: String,
    val customerMobile: String
)

data class CreateGatewayOrderResponse(
    val gatewayOrderId: String,
    val amountPaise: Int,
    val currency: String
)

data class VerifyGatewayPaymentRequest(
    val orderNumber: String,
    val gatewayOrderId: String,
    val gatewayPaymentId: String,
    val gatewaySignature: String
)

data class VerifyGatewayPaymentResponse(
    val verified: Boolean,
    val status: String,
    val message: String
)

data class NotificationWebhookPayload(
    val eventType: String,
    val orderNumber: String,
    val customerEmail: String,
    val customerMobile: String,
    val title: String,
    val message: String
)

interface PaymentBackendApi {
    @POST("api/payments/create-order")
    suspend fun createOrder(@Body request: CreateGatewayOrderRequest): Response<CreateGatewayOrderResponse>

    @POST("api/payments/verify-signature")
    suspend fun verifyPayment(@Body request: VerifyGatewayPaymentRequest): Response<VerifyGatewayPaymentResponse>
}

interface NotificationWebhookApi {
    @POST("notify")
    suspend fun dispatchNotification(@Body payload: NotificationWebhookPayload): Response<Unit>
}

sealed interface GatewayExecutionResult {
    data class Verified(
        val paymentStatus: PaymentStatus,
        val paymentReference: String,
        val message: String
    ) : GatewayExecutionResult

    data class PendingVerification(
        val paymentStatus: PaymentStatus,
        val paymentReference: String,
        val message: String
    ) : GatewayExecutionResult

    data class Failed(val reason: String) : GatewayExecutionResult
}

data class GatewayConfigStatus(
    val razorpayKeyConfigured: Boolean,
    val paymentBackendConfigured: Boolean,
    val notificationWebhookConfigured: Boolean,
    val razorpayKeyPreview: String
)

class PaymentGatewayManager {

    fun getConfigStatus(): GatewayConfigStatus {
        val rawKey = BuildConfig.RAZORPAY_KEY_ID.trim()
        val keyId = if (rawKey.startsWith("YOUR_")) "" else rawKey
        val backendUrl = BuildConfig.PAYMENT_BACKEND_URL.trim()
        val notifyUrl = BuildConfig.NOTIFICATION_WEBHOOK_URL.trim()
        return GatewayConfigStatus(
            razorpayKeyConfigured = keyId.isNotBlank(),
            paymentBackendConfigured = backendUrl.startsWith("http://") || backendUrl.startsWith("https://"),
            notificationWebhookConfigured = notifyUrl.startsWith("http://") || notifyUrl.startsWith("https://"),
            razorpayKeyPreview = if (keyId.length > 8) "${keyId.take(8)}••••" else if (keyId.isNotBlank()) "Configured" else "Not configured"
        )
    }

    private fun buildClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
    }

    suspend fun processOnlinePayment(
        orderNumber: String,
        amountInr: Int,
        paymentMethod: PaymentMethod,
        customerEmail: String,
        customerMobile: String,
        useTestMode: Boolean,
        testCredentialInput: String
    ): GatewayExecutionResult {
        val config = getConfigStatus()

        if (config.razorpayKeyConfigured && config.paymentBackendConfigured && !useTestMode) {
            return try {
                val baseUrl = BuildConfig.PAYMENT_BACKEND_URL.trim().let {
                    if (it.endsWith("/")) it else "$it/"
                }
                val api = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(buildClient())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(PaymentBackendApi::class.java)

                val createResp = api.createOrder(
                    CreateGatewayOrderRequest(
                        orderNumber = orderNumber,
                        amountPaise = amountInr * 100,
                        paymentMethod = paymentMethod.key,
                        customerEmail = customerEmail,
                        customerMobile = customerMobile
                    )
                )
                if (!createResp.isSuccessful || createResp.body() == null) {
                    return GatewayExecutionResult.Failed(
                        "Payment gateway server returned HTTP ${createResp.code()}. Order was not marked paid."
                    )
                }
                val gatewayOrder = createResp.body()!!
                GatewayExecutionResult.PendingVerification(
                    paymentStatus = PaymentStatus.PENDING_GATEWAY_VERIFICATION,
                    paymentReference = gatewayOrder.gatewayOrderId,
                    message = "Gateway order ${gatewayOrder.gatewayOrderId} created. Awaiting webhook/signature confirmation."
                )
            } catch (e: Exception) {
                GatewayExecutionResult.Failed(
                    "Unable to reach payment verification server: ${e.localizedMessage ?: "Network error"}"
                )
            }
        }

        // Sandbox / Test Mode (Explicitly labeled; never pretends to be a live server-verified charge)
        if (useTestMode) {
            val trimmedCred = testCredentialInput.trim()
            if (trimmedCred.equals("failure@razorpay", ignoreCase = true) ||
                trimmedCred.equals("FAIL", ignoreCase = true)
            ) {
                return GatewayExecutionResult.Failed(
                    "Gateway Test Mode simulated a declined transaction ($trimmedCred). No order was placed."
                )
            }
            if (trimmedCred.isBlank()) {
                return GatewayExecutionResult.Failed(
                    "Enter a test UPI ID (e.g., success@razorpay) or test gateway reference token to run Test Mode."
                )
            }
            val sandboxRef = "TEST_RZP_${orderNumber}_${System.currentTimeMillis() % 100000}"
            return GatewayExecutionResult.Verified(
                paymentStatus = PaymentStatus.PAID_TEST_MODE,
                paymentReference = "$sandboxRef ($trimmedCred)",
                message = "Authorized via Razorpay/Cashfree Sandbox Test Mode. Configure RAZORPAY_KEY_ID & PAYMENT_BACKEND_URL in AI Studio Secrets for live payments."
            )
        }

        return GatewayExecutionResult.Failed(
            "Live payment gateway credentials (RAZORPAY_KEY_ID & PAYMENT_BACKEND_URL) are not configured. Enable Gateway Test Mode to verify test checkout, or choose Cash on Delivery."
        )
    }

    suspend fun dispatchWebhookNotificationIfConfigured(
        payload: NotificationWebhookPayload
    ): String {
        val notifyUrl = BuildConfig.NOTIFICATION_WEBHOOK_URL.trim()
        if (!notifyUrl.startsWith("http://") && !notifyUrl.startsWith("https://")) {
            return "In-App Saved | Email/SMS/WhatsApp webhook not configured (NOTIFICATION_WEBHOOK_URL unset)"
        }
        return try {
            val baseUrl = if (notifyUrl.endsWith("/")) notifyUrl else "$notifyUrl/"
            val api = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildClient())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(NotificationWebhookApi::class.java)
            val resp = api.dispatchNotification(payload)
            if (resp.isSuccessful) {
                "In-App Saved | External webhook accepted (HTTP ${resp.code()})"
            } else {
                "In-App Saved | Webhook returned HTTP ${resp.code()}"
            }
        } catch (e: Exception) {
            "In-App Saved | Webhook unreachable (${e.localizedMessage ?: "error"})"
        }
    }
}
