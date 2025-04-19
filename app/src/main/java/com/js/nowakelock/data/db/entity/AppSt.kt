package com.js.nowakelock.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "appSt",primaryKeys = ["packageName_st","userId_appSt"])
data class AppSt(
    @ColumnInfo(name = "packageName_st")
    var packageName: String = "",
    var wakelock: Boolean = false,
    var alarm: Boolean = false,
    var service: Boolean = false,
    var rE_Wakelock: Set<String> = mutableSetOf(),
    var rE_Alarm: Set<String> = mutableSetOf(),
    var rE_Service: Set<String> = mutableSetOf(),
    @ColumnInfo(name = "userId_appSt",defaultValue = "0")
    var userId: Int = 0,
)
