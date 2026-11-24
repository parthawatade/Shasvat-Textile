package com.example.data.domain

import com.example.data.model.CartItemDetail
import com.example.data.model.DeliveryMethod
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OfferEvaluation
import com.example.data.model.PriceBreakdown
import com.example.data.model.Product
import kotlin.math.max
import kotlin.math.roundToInt

object PricingEngine {

    const val SHOPPING_BAG_CHARGE_INR = 20

    data class GstTaxResult(
        val taxableAmount: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val igstAmount: Double,
        val totalTax: Double
    )

    fun evaluateOffers(
        subtotal: Int,
        offers: List<OfferEntity>,
        deliverySettings: DeliverySettingsEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): List<OfferEvaluation> {
        return offers
            .filter { it.isActive && (it.expiryDateMillis == null || it.expiryDateMillis > nowMillis) }
            .sortedBy { it.minOrderAmount }
            .map { offer ->
                val eligible = subtotal > offer.minOrderAmount
                val shortBy = if (eligible) 0 else (offer.minOrderAmount + 1) - subtotal
                val deliverySavings = if (offer.includesFreeStandardDelivery && subtotal <= deliverySettings.freeDeliveryMinSubtotal) {
                    deliverySettings.standardCharge
                } else 0
                val giftValueHint = if (offer.freeGiftMaxPrice != null) 250 else 0
                val estimatedSavings = offer.discountAmount + deliverySavings + giftValueHint
                OfferEvaluation(
                    offer = offer,
                    isEligible = eligible,
                    amountShort = max(0, shortBy),
                    estimatedSavings = estimatedSavings
                )
            }
    }

    fun selectBestEligibleOffer(
        subtotal: Int,
        offers: List<OfferEntity>,
        deliverySettings: DeliverySettingsEntity,
        preferredOfferId: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): OfferEntity? {
        val evaluations = evaluateOffers(subtotal, offers, deliverySettings, nowMillis)
        val eligible = evaluations.filter { it.isEligible }
        if (eligible.isEmpty()) return null

        val preferred = preferredOfferId?.let { id ->
            eligible.firstOrNull { it.offer.id == id }?.offer
        }
        if (preferred != null) return preferred

        return eligible.maxWithOrNull(
            compareBy<OfferEvaluation> { it.estimatedSavings }
                .thenBy { it.offer.minOrderAmount }
        )?.offer
    }

    fun calculatePriceBreakdown(
        cartItems: List<CartItemDetail>,
        offers: List<OfferEntity>,
        deliverySettings: DeliverySettingsEntity,
        preferredOfferId: String?,
        deliveryMethod: DeliveryMethod,
        isPinRushEligible: Boolean,
        selectedFreeGift: Product?,
        nowMillis: Long = System.currentTimeMillis()
    ): PriceBreakdown {
        if (cartItems.isEmpty()) {
            return PriceBreakdown(
                productSubtotal = 0,
                offerDiscount = 0,
                standardDeliveryCharge = 0,
                rushDeliveryCharge = 0,
                totalDeliveryCharge = 0,
                shoppingBagCharge = 0,
                finalPayableAmount = 0,
                appliedOffer = null,
                isFreeStandardDeliveryApplied = false,
                isFreeGiftEligible = false,
                freeGiftMaxPrice = null,
                selectedFreeGift = null,
                isRushAvailableForPin = isPinRushEligible,
                estimatedDeliveryText = deliverySettings.standardEstimateText
            )
        }

        val subtotal = cartItems.sumOf { it.lineTotal }
        val appliedOffer = selectBestEligibleOffer(
            subtotal = subtotal,
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = preferredOfferId,
            nowMillis = nowMillis
        )

        val qualifiesFreeDeliveryBySubtotal = subtotal > deliverySettings.freeDeliveryMinSubtotal
        val qualifiesFreeDeliveryByOffer = appliedOffer?.includesFreeStandardDelivery == true
        val isFreeStandardDelivery = qualifiesFreeDeliveryBySubtotal || qualifiesFreeDeliveryByOffer

        val standardDeliveryCharge = if (isFreeStandardDelivery) 0 else deliverySettings.standardCharge

        val useRush = deliveryMethod == DeliveryMethod.RUSH && isPinRushEligible
        val rushDeliveryCharge = if (useRush) deliverySettings.rushExtraCharge else 0
        val totalDeliveryCharge = standardDeliveryCharge + rushDeliveryCharge

        val rawDiscount = appliedOffer?.discountAmount ?: 0
        val cappedDiscount = rawDiscount.coerceAtMost(subtotal)

        val bagCharge = SHOPPING_BAG_CHARGE_INR

        val rawFinal = (subtotal - cappedDiscount) + totalDeliveryCharge + bagCharge
        val finalPayable = max(0, rawFinal)

        val freeGiftMaxPrice = appliedOffer?.freeGiftMaxPrice
        val isFreeGiftEligible = freeGiftMaxPrice != null
        val validatedGift = if (isFreeGiftEligible && selectedFreeGift != null && selectedFreeGift.price < freeGiftMaxPrice) {
            selectedFreeGift
        } else null

        val estimateText = if (useRush) {
            deliverySettings.rushEstimateText
        } else {
            deliverySettings.standardEstimateText
        }

        return PriceBreakdown(
            productSubtotal = subtotal,
            offerDiscount = cappedDiscount,
            standardDeliveryCharge = standardDeliveryCharge,
            rushDeliveryCharge = rushDeliveryCharge,
            totalDeliveryCharge = totalDeliveryCharge,
            shoppingBagCharge = bagCharge,
            finalPayableAmount = finalPayable,
            appliedOffer = appliedOffer,
            isFreeStandardDeliveryApplied = isFreeStandardDelivery,
            isFreeGiftEligible = isFreeGiftEligible,
            freeGiftMaxPrice = freeGiftMaxPrice,
            selectedFreeGift = validatedGift,
            isRushAvailableForPin = isPinRushEligible,
            estimatedDeliveryText = estimateText
        )
    }

    fun calculateGstTax(
        finalPayableAmount: Int,
        gstRatePercent: Double,
        sellerState: String,
        customerState: String
    ): GstTaxResult {
        if (finalPayableAmount <= 0 || gstRatePercent <= 0.0) {
            return GstTaxResult(
                taxableAmount = finalPayableAmount.toDouble(),
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = 0.0,
                totalTax = 0.0
            )
        }
        val divisor = 1.0 + (gstRatePercent / 100.0)
        val taxable = ((finalPayableAmount / divisor) * 100.0).roundToInt() / 100.0
        val totalTax = ((finalPayableAmount - taxable) * 100.0).roundToInt() / 100.0
        val isIntraState = sellerState.trim().equals(customerState.trim(), ignoreCase = true) ||
            customerState.isBlank()

        return if (isIntraState) {
            val half = ((totalTax / 2.0) * 100.0).roundToInt() / 100.0
            val otherHalf = ((totalTax - half) * 100.0).roundToInt() / 100.0
            GstTaxResult(
                taxableAmount = taxable,
                cgstAmount = half,
                sgstAmount = otherHalf,
                igstAmount = 0.0,
                totalTax = totalTax
            )
        } else {
            GstTaxResult(
                taxableAmount = taxable,
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = totalTax,
                totalTax = totalTax
            )
        }
    }
}
