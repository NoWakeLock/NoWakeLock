package com.js.nowakelock.data.repository.da

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.DA
import com.js.nowakelock.data.db.entity.St
import kotlinx.coroutines.flow.Flow

//interface DaRepo {
//    /**
//     * get a signal of da
//     * @param name String
//     * @param type Type
//     * @return Flow<DA>
//     */
//    fun getDa(name: String, type: Type, userId: Int): Flow<DA>
//
//    /**
//     * install st
//     * @param st St
//     */
//    suspend fun insertSt(st: St)
//}