package com.mitch.syncwork.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mitch.syncwork.data.auth.AuthRepository
import com.mitch.syncwork.data.auth.UserPrefsDataSource

class DefaultDependenciesProvider(private val context: Context) : DependenciesProvider {
    private val Context.userPrefsDatastore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    override val userPrefsDataSource by lazy {
        UserPrefsDataSource(userPrefsDatastore = context.userPrefsDatastore)
    }
    override val authRepository: AuthRepository by lazy {
        AuthRepository(userPrefsDataSource = userPrefsDataSource)
    }
}
