package com.mitch.syncwork

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.mitch.syncwork.data.sync.SyncManager
import com.mitch.syncwork.data.sync.SyncManagerFactory
import com.mitch.syncwork.di.DefaultDependenciesProvider
import com.mitch.syncwork.di.DependenciesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val context = this
        applicationScope.launch {
            dependenciesProvider.authRepository.isUserLoggedIn.distinctUntilChanged().collect { isLoggedIn ->
                Log.d("SyncApplication", "Login status changed to: $isLoggedIn")
                if (isLoggedIn) {
                    Log.d("SyncApplication", "User is now logged in. Starting sync manager")
                    SyncManager.startSync(context)
                } else {
                    Log.d("SyncApplication", "User is now logged out. Cancelling sync manager")
                    SyncManager.stopSync(context)
                }
            }
        }
    }
}
