package org.lsm.flower_mailing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun saveLoginData(token: String, refreshToken: String, role: String, name: String, email: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = token
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            preferences[PreferencesKeys.USER_ROLE] = role
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    val userRoleFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ROLE]
        }

    val accessTokenFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN]
        }

    val refreshTokenFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }

    val userNameFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME]
        }

    val userEmailFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL]
        }

    suspend fun clearLoginData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}