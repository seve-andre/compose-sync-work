package com.mitch.syncwork

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mitch.syncwork.data.auth.SyncRate
import com.mitch.syncwork.data.auth.UserPrefsDataSource
import com.mitch.syncwork.data.sync.SyncManager
import com.mitch.syncwork.data.sync.SyncManager.Companion.WorkTag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel(
    userPrefsDataSource: UserPrefsDataSource,
    private val workManager: WorkManager,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = userPrefsDataSource.data
        .map { prefs ->
            MainActivityUiState.Success(isLoggedIn = prefs.isLoggedIn, syncRate = prefs.syncRate)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    private val _currentSyncRequest = MutableStateFlow<OneTimeWorkRequest?>(null)
    private val currentSyncRequest = _currentSyncRequest.asStateFlow()
    val syncState = currentSyncRequest.flatMapLatest { request ->
        if (request != null) {
            workManager.getWorkInfoByIdFlow(request.id)
                .map { workInfo ->
                    Log.d("MainActivityViewModel", "WorkInfo: $workInfo")
                    when (workInfo?.state) {
                        WorkInfo.State.RUNNING -> {
                            val progress = workInfo.progress.getInt(SyncManager.Progress, 0)
                            SyncState.Syncing(progress = progress)
                        }

                        WorkInfo.State.SUCCEEDED -> SyncState.SyncComplete
                        WorkInfo.State.FAILED -> SyncState.SyncFailed
                        else -> SyncState.Idle
                    }
                }
        } else {
            flowOf(SyncState.Idle)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncState.Idle
    )

    fun startSync() {
        _currentSyncRequest.value = OneTimeWorkRequestBuilder<SyncManager>()
            .addTag(WorkTag)
            .build()
        workManager.enqueue(_currentSyncRequest.value!!)
    }

    fun stopSync() {
        _currentSyncRequest.value?.let {
            workManager.cancelWorkById(it.id)
        }
        _currentSyncRequest.value = null
    }

    fun syncNow() {
        stopSync()
        startSync()
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val isLoggedIn: Boolean, val syncRate: SyncRate) : MainActivityUiState
    data object Error : MainActivityUiState
}
