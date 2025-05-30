package com.mitch.syncwork

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.mitch.syncwork.data.sync.SyncManagerFactory
import com.mitch.syncwork.di.DefaultDependenciesProvider
import com.mitch.syncwork.di.DependenciesProvider

class SyncApplication : Application(), Configuration.Provider {

    lateinit var dependenciesProvider: DependenciesProvider
    override fun onCreate() {
        super.onCreate()
        dependenciesProvider = DefaultDependenciesProvider(this)
    }

    override val workManagerConfiguration: Configuration
        get() {
            val workManagerFactory = DelegatingWorkerFactory().apply {
                addFactory(SyncManagerFactory(userPrefsDataSource = dependenciesProvider.userPrefsDataSource))
            }
            return Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .setWorkerFactory(workManagerFactory)
                .build()
        }

}
