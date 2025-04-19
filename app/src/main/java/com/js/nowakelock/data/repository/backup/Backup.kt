package com.js.nowakelock.data.repository.backup

import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    var appSts: List<AppSt> = mutableListOf(),
    var sts: List<St> = mutableListOf()
)
