package com.js.nowakelock.data.repository.daitem

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.config.ConfigPublisher
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao

class WakelockRepositoryImpl(
    daDao: DADao,
    infoEventDao: InfoEventDao,
    configPublisher: ConfigPublisher? = null
) : DARepositoryImpl(daDao, infoEventDao, configPublisher) {
    override val type = Type.Wakelock
}

class AlarmRepositoryImpl(
    daDao: DADao,
    infoEventDao: InfoEventDao,
    configPublisher: ConfigPublisher? = null
) : DARepositoryImpl(daDao, infoEventDao, configPublisher) {
    override val type = Type.Alarm
}

class ServiceRepositoryImpl(
    daDao: DADao,
    infoEventDao: InfoEventDao,
    configPublisher: ConfigPublisher? = null
) : DARepositoryImpl(daDao, infoEventDao, configPublisher) {
    override val type = Type.Service
}
