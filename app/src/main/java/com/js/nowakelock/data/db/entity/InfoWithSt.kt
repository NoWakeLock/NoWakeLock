package com.js.nowakelock.data.db.entity

import androidx.room.Embedded

data class InfoWithSt(
    @Embedded
    var info: Info,
    @Embedded
    var st: St?
)
