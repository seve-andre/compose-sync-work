package com.mitch.syncwork.di

import androidx.work.WorkManager
import com.mitch.syncwork.data.auth.AuthRepository
import com.mitch.syncwork.data.auth.UserPrefsDataSource

interface DependenciesProvider {
    val userPrefsDataSource: UserPrefsDataSource
    val authRepository: AuthRepository
    val workManager: WorkManager
}
