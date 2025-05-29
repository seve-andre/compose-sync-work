package com.mitch.syncwork.data.sync

import com.mitch.syncwork.data.auth.SyncRate

data class SyncData(val isLoggedIn: Boolean, val syncRate: SyncRate)
