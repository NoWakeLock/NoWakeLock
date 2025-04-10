package com.js.nowakelock.data.repository.da

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.DA
import com.js.nowakelock.data.db.entity.St
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip

//class DaR(private val daDao: DADao) : DaRepo {
//    /**
//     * get a signal of da
//     * @param name String
//     * @param type Type
//     * @return Flow<DA>
//     */
//    override fun getDa(name: String, type: Type, userId: Int): Flow<DA> {
//        val info = daDao.loadInfo(name, type, userId)
//        val st = daDao.loadSt(name, type, userId)
//        return info.zip(st) { i, s -> //merge info and st
//            if (i.count != 0) // calculate blockCountTime
//                i.blockCountTime = i.blockCount * (i.countTime / i.count)
//
//            DA(i, s ?: St(i.name, i.type, i.packageName, userId = i.userId))
//        }
//    }
//
//    /**
//     * install st
//     * @param st St
//     */
//    override suspend fun insertSt(st: St) {
//        daDao.insert(st)
//    }
//}