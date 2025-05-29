package com.mitch.syncwork

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mitch.syncwork.data.auth.SyncRate
import com.mitch.syncwork.data.sync.SyncManager
import com.mitch.syncwork.ui.screens.login.FakeAuthRoute
import com.mitch.syncwork.ui.theme.SyncWorkTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val dependenciesProvider = (application as SyncApplication).dependenciesProvider

        setContent {
            SyncWorkTheme {
                @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                Scaffold { _ ->
                    val scope = rememberCoroutineScope()
                    var syncRate by remember { mutableStateOf(SyncRate.Minutes1) }
                    LaunchedEffect(Unit) {
                        dependenciesProvider.userPrefsDataSource.data.collectLatest {
                            syncRate = it.syncRate
                        }
                    }

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
                        onSyncNow = {
                            SyncManager.syncNow(applicationContext)
                        },
                        selectedSyncRate = syncRate
                    )
                }
            }
        }
    }
}
