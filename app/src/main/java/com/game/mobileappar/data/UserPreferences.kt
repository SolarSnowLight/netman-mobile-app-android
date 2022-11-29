package com.game.mobileappar.data

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import com.game.mobileappar.constants.values.store.DataKeyConstants
import com.game.mobileappar.constants.values.store.DataStoreNameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(
    context: Context
) {
    private val appContext = context.applicationContext

    // Создание локального хранилища
    private val dataStore : DataStore<Preferences> = appContext.createDataStore(
        name = DataStoreNameConstants.AUTH_STORE
    )

    // Представление данных в виде асинхронного потока
    val authData : Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH]
        }

    // Сохранение авторизационных данных пользователя
    suspend fun saveAuthData(authData: String){
        dataStore.edit { preferences ->
            preferences[KEY_AUTH] = authData
        }
    }

    // Очистка хранилища
    suspend fun clear(){
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Ключ для доступа к данным
    companion object {
        private val KEY_AUTH = preferencesKey<String>(DataKeyConstants.KEY_AUTH)
    }
}