package com.js.nowakelock.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.js.nowakelock.data.db.Type
import java.io.Serializable

@Entity(
    tableName = "info_event",
    indices = [
        Index(
            value = ["packageName_event", "type_event", "startTime"],
            name = "index_info_event_package_type_time"
        ),
        Index(
            value = ["name_event", "type_event", "userId_event"],
            name = "index_info_event_name_type_userId"
        ),
        Index(
            value = ["eventKey"],
            name = "index_info_event_key",
            unique = true
        )
    ]
)
data class InfoEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name_event")
    var name: String = "",
    @ColumnInfo(name = "type_event")
    var type: Type = Type.UnKnow,
    @ColumnInfo(name = "packageName_event")
    var packageName: String = "",
    @ColumnInfo(name = "userId_event", defaultValue = "0")
    var userId: Int = 0,
    @ColumnInfo(name = "startTime")
    var startTime: Long = 0,
    @ColumnInfo(name = "endTime")
    var endTime: Long? = null,
    @ColumnInfo(name = "isBlocked")
    var isBlocked: Boolean = false,
    @ColumnInfo(name = "eventKey")
    var eventKey: String = ""
) : Serializable {
    companion object {
        @JvmStatic private val serialVersionUID = 2356751629234196467L
        
        /**
         * 生成事件唯一键
         */
        @JvmStatic
        fun generateEventKey(name: String, packageName: String, type: Type, userId: Int, startTime: Long): String {
            return "${packageName}_${name}_${type.value}_${userId}_${startTime}"
        }
    }
} 