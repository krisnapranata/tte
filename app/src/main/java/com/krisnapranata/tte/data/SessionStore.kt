package com.krisnapranata.tte.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.krisnapranata.tte.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tte_session")

data class Session(
    val serverUrl: String,
    val nik: String,
    val nama: String,
)

class SessionStore(private val context: Context) {

    private val keyServer = stringPreferencesKey("server_url")
    private val keyNik = stringPreferencesKey("nik")
    private val keyNama = stringPreferencesKey("nama")

    val session: Flow<Session> = context.dataStore.data.map { prefs ->
        Session(
            serverUrl = prefs[keyServer] ?: BuildConfig.API_BASE_URL,
            nik = prefs[keyNik] ?: "",
            nama = prefs[keyNama] ?: "",
        )
    }

    suspend fun saveServer(url: String) {
        context.dataStore.edit { it[keyServer] = url }
    }

    suspend fun saveUser(nik: String, nama: String) {
        context.dataStore.edit {
            it[keyNik] = nik
            it[keyNama] = nama
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit {
            it.remove(keyNik)
            it.remove(keyNama)
        }
    }
}
