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
import com.mitch.syncwork.data.auth.SyncRate

@Composable
fun FakeAuthRoute(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSyncRateChange: (SyncRate) -> Unit,
    onSyncNow: () -> Unit,
    selectedSyncRate: SyncRate
) {
    FakeAuthScreen(
        onLogin = onLogin,
        onLogout = onLogout,
        onSyncRateChange = onSyncRateChange,
        onSyncNow = onSyncNow,
        selectedSyncRate = selectedSyncRate
    )
}

@Composable
private fun FakeAuthScreen(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSyncRateChange: (SyncRate) -> Unit,
    onSyncNow: () -> Unit,
    selectedSyncRate: SyncRate
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
