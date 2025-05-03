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
        // for DADetail
        Index(
            value = ["name_event", "type_event", "userId_event", "startTime"],
            name = "index_info_event_name_type_userId_time"
        )
    ]
)
data class InfoEvent(
    @PrimaryKey
    @ColumnInfo(name = "instanceId", defaultValue = "")
    var instanceId: String = "",  // IBinder hash + timestamp
    
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
) : Serializable {
    companion object {
        @JvmStatic
        private val serialVersionUID = 2356751629234196468L  // for Serializable

        /**
         * generate instance id
         */
        @JvmStatic
        fun generateInstanceId(
            iBinderHash: String,
            timestamp: Long
        ): String {
            return "${iBinderHash}_${timestamp}"
        }
    }
} 