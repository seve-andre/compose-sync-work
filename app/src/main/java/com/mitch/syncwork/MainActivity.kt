package com.mitch.syncwork

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkManager
import com.mitch.syncwork.data.sync.SyncData
import com.mitch.syncwork.ui.screens.login.FakeAuthRoute
import com.mitch.syncwork.ui.theme.SyncWorkTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val dependenciesProvider = (application as SyncApplication).dependenciesProvider
        val viewModel = MainActivityViewModel(
            userPrefsDataSource = dependenciesProvider.userPrefsDataSource,
            workManager = WorkManager.getInstance(applicationContext)
        )
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .onEach { uiState = it }
                        .collect()
                }

                launch {
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
                            viewModel.stopSync()
                            if (data.isLoggedIn) {
                                Log.d("SyncApplication", "User is now logged in. Starting sync manager")
                                viewModel.startSync()
                            }
                        }
                }
            }
        }

        setContent {

            SyncWorkTheme {
                @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                Scaffold { _ ->
                    val scope = rememberCoroutineScope()
                    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

                    LaunchedEffect(syncState) {
                        Log.d("MainActivity", "Sync state: $syncState")
                        if (syncState is SyncState.SyncComplete) {
                            Toast.makeText(applicationContext, "Sync complete", Toast.LENGTH_SHORT).show()
                        }
                    }

                    when (uiState) {
                        MainActivityUiState.Error -> Unit
                        MainActivityUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is MainActivityUiState.Success -> {
                            val successState = uiState as MainActivityUiState.Success
                            FakeAuthRoute(
                                onLogin = {
                                    scope.launch {
                                        dependenciesProvider.authRepository.login()
                                    }
                                },
                                onLogout = {
                                    scope.launch {
                                        dependenciesProvider.authRepository.logout()
                                    }
                                },
                                onSyncRateChange = { newSyncRate ->
                                    scope.launch {
                                        dependenciesProvider.userPrefsDataSource.saveSyncRate(newSyncRate)
                                    }
                                },
                                onSyncNow = viewModel::syncNow,
                                selectedSyncRate = successState.syncRate,
                                syncState = syncState
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface SyncState {
    data object Idle : SyncState
    data class Syncing(@IntRange(from = 0, to = 100) val progress: Int) : SyncState
    data object SyncComplete : SyncState
    data object SyncFailed : SyncState
}
