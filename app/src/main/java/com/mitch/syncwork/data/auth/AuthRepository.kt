package com.mitch.syncwork.data.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

class AuthRepository(private val userPrefsDataSource: UserPrefsDataSource) {
    val isUserLoggedIn = userPrefsDataSource.data.map { it.isLoggedIn }

    suspend fun login() {
        delay(300)
        userPrefsDataSource.saveIsUserLoggedIn(true)
    }

    suspend fun logout() {
        userPrefsDataSource.saveIsUserLoggedIn(false)
    }
}
