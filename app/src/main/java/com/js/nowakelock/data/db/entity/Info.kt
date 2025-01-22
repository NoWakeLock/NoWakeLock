package com.js.nowakelock.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.js.nowakelock.data.db.Type
import java.io.Serializable

@Entity(tableName = "info", primaryKeys = ["name_info", "type_info", "userid_info"])
data class Info(

    @ColumnInfo(name = "name_info")
    var name: String = "",
    @ColumnInfo(name = "type_info")
    var type: Type = Type.UnKnow,
    @ColumnInfo(name = "packageName_info")
    var packageName: String = "",
    var count: Int = 0,
    var blockCount: Int = 0,
    var countTime: Long = 0,
    @ColumnInfo(name = "userid_info", defaultValue = "0")
    var userId: Int = 0,
) : Serializable {
    companion object {
        @JvmStatic private val serialVersionUID = 3245751629200196467L
    }

    @Ignore
    var blockCountTime: Long = 0
}
