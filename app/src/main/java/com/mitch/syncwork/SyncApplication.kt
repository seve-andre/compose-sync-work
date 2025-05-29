package com.mitch.syncwork

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.mitch.syncwork.data.sync.SyncData
import com.mitch.syncwork.data.sync.SyncManager
import com.mitch.syncwork.data.sync.SyncManagerFactory
import com.mitch.syncwork.di.DefaultDependenciesProvider
import com.mitch.syncwork.di.DependenciesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SyncApplication : Application(), Configuration.Provider {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var dependenciesProvider: DependenciesProvider
    override fun onCreate() {
        super.onCreate()
        dependenciesProvider = DefaultDependenciesProvider(this)
        observeLoginStatusAndManageWorker()
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

    private fun observeLoginStatusAndManageWorker() {
        applicationScope.launch {
            /**
             * Stops the current sync and conditionally restarts it based on user preferences.
             * - Sync is stopped if user is NOT logged in.
             * - Sync is started if user is logged in.
             * - Changes to sync rate or login status trigger a stop then immediate restart (if logged in).
             */
            dependenciesProvider.userPrefsDataSource.data
                .map { prefs -> SyncData(isLoggedIn = prefs.isLoggedIn, syncRate = prefs.syncRate) }
                .distinctUntilChanged()
                .collect { data ->
                    Log.d("SyncApplication", "isLoggedIn? ${data.isLoggedIn}")
                    Log.d("SyncApplication", "current sync rate: ${data.syncRate.name}")
                    SyncManager.stopSync(applicationContext)
                    if (data.isLoggedIn) {
                        Log.d("SyncApplication", "User is now logged in. Starting sync manager")
                        SyncManager.startSync(applicationContext)
                    }
                }
        }
    }
}
