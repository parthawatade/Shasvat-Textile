package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "shasvat_session")

class SessionPreferences(private val context: Context) {

    private object Keys {
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val SELECTED_PIN_CODE = stringPreferencesKey("selected_pin_code")
        val SELECTED_OFFER_ID = stringPreferencesKey("selected_offer_id")
    }

    companion object {
        const val GUEST_USER_ID = "guest"
    }

    val activeUserId: Flow<String> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_USER_ID] ?: GUEST_USER_ID
    }

    val selectedPinCode: Flow<String> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_PIN_CODE] ?: ""
    }

    val selectedOfferId: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_OFFER_ID]?.takeIf { it.isNotBlank() }
    }

    suspend fun setActiveUserId(userId: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_USER_ID] = userId.ifBlank { GUEST_USER_ID }
        }
    }

    suspend fun setSelectedPinCode(pinCode: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.SELECTED_PIN_CODE] = pinCode.trim()
        }
    }

    suspend fun setSelectedOfferId(offerId: String?) {
        context.sessionDataStore.edit { prefs ->
            if (offerId.isNullOrBlank()) {
                prefs.remove(Keys.SELECTED_OFFER_ID)
            } else {
                prefs[Keys.SELECTED_OFFER_ID] = offerId
            }
        }
    }
}
