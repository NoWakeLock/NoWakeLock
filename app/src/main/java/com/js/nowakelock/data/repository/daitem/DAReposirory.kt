package com.js.nowakelock.data.repository.daitem

import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao

class WakelockRepositoryImpl(daDao: DADao, infoEventDao: InfoEventDao) :
    DARepositoryImpl(daDao, infoEventDao) {
    override val type = com.js.nowakelock.data.db.Type.Wakelock
}

class AlarmRepositoryImpl(daDao: DADao, infoEventDao: InfoEventDao) :
    DARepositoryImpl(daDao, infoEventDao) {
    override val type = com.js.nowakelock.data.db.Type.Alarm
}

class ServiceRepositoryImpl(daDao: DADao, infoEventDao: InfoEventDao) :
    DARepositoryImpl(daDao, infoEventDao) {
    override val type = com.js.nowakelock.data.db.Type.Service
}