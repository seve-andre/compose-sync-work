package com.mitch.syncwork.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mitch.syncwork.SyncState
import com.mitch.syncwork.data.auth.SyncRate

@Composable
fun FakeAuthRoute(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSyncRateChange: (SyncRate) -> Unit,
    onSyncNow: () -> Unit,
    selectedSyncRate: SyncRate,
    syncState: SyncState
) {
    FakeAuthScreen(
        onLogin = onLogin,
        onLogout = onLogout,
        onSyncRateChange = onSyncRateChange,
        onSyncNow = onSyncNow,
        selectedSyncRate = selectedSyncRate,
        syncState = syncState
    )
}

@Composable
private fun FakeAuthScreen(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSyncRateChange: (SyncRate) -> Unit,
    onSyncNow: () -> Unit,
    selectedSyncRate: SyncRate,
    syncState: SyncState
) {
    var currentDialog: FakeAuthScreenDialog? by remember { mutableStateOf(null) }

    when (currentDialog) {
        FakeAuthScreenDialog.SyncRate -> SyncRateDialog(
            onDismiss = { currentDialog = null },
            onSyncRateChange = onSyncRateChange,
            selectedSyncRate = selectedSyncRate
        )

        null -> Unit
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Sync state: ")
                }
                val text = when (syncState) {
                    SyncState.Idle -> "Idle"
                    SyncState.SyncComplete -> "Done"
                    SyncState.SyncFailed -> "Failed"
                    is SyncState.Syncing -> "progress ${syncState.progress}%"
                }
                append(text)
            }
        )
        Button(onClick = onLogin) {
            Text("Login")
        }
        Button(onClick = onLogout) {
            Text("Logout")
        }

        Button(onClick = { currentDialog = FakeAuthScreenDialog.SyncRate }) {
            Text("Change sync rate")
        }

        Button(onClick = onSyncNow) {
            Text("Sync now")
        }
    }
}

private enum class FakeAuthScreenDialog {
    SyncRate
}
