package com.js.nowakelock.data.repository.daitem

import com.js.nowakelock.data.db.dao.DADao

class WakelockRepositoryImpl(daDao: DADao) : DARepositoryImpl(daDao) {
    override val type = com.js.nowakelock.data.db.Type.Wakelock
}

class AlarmRepositoryImpl(daDao: DADao) : DARepositoryImpl(daDao) {
    override val type = com.js.nowakelock.data.db.Type.Alarm
}

class ServiceRepositoryImpl(daDao: DADao) : DARepositoryImpl(daDao) {
    override val type = com.js.nowakelock.data.db.Type.Service
}