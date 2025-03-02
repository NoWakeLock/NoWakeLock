package com.js.nowakelock.data.repository.das

import android.os.Bundle
import com.js.nowakelock.BasicApp
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.DA
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

open class IFR(private val daDao: DADao) : FR {

    open val type: Type = Type.UnKnow

    private val tag = "NoWakelock"

    override fun getDAs(): Flow<List<DA>> {
        return daDao.loadISs(type).distinctUntilChanged().map { map ->
            val list = mutableListOf<DA>()

            map.forEach {
                if (it.key.count != 0)// calculate blockCountTime
                    it.key.blockCountTime =
                        it.key.blockCount * (it.key.countTime / it.key.count)

                if (it.value == null) {
                    list.add(
                        DA(
                            info = it.key,
                            st = St(
                                name = it.key.name, type = it.key.type,
                                packageName = it.key.packageName, userId = it.key.userId
                            )
                        )
                    )
                } else {
                    list.add(DA(info = it.key, st = it.value))
                }
            }
            list
        }
    }

    override fun getDAs(packageName: String, userId: Int): Flow<List<DA>> {
        return daDao.loadISs(packageName, type, userId).distinctUntilChanged().map { map ->
            val list = mutableListOf<DA>()
            map.forEach {
                if (it.key.count != 0)// calculate blockCountTime
                    it.key.blockCountTime =
                        it.key.blockCount * (it.key.countTime / it.key.count)

                if (it.value == null) {
                    list.add(
                        DA(
                            info = it.key,
                            st = St(
                                name = it.key.name, type = it.key.type,
                                packageName = it.key.packageName, userId = it.key.userId
                            )
                        )
                    )
                } else {
                    list.add(DA(info = it.key, st = it.value))
                }
            }
            list
        }
    }

    override suspend fun insertInfos(infos: List<Info>) {
        daDao.insert(infos)
    }

    override suspend fun insertSt(st: St) {
        daDao.insert(st)
    }

    override suspend fun getSts(): Flow<List<St>> {
        return daDao.loadSts(type).distinctUntilChanged()
    }

    override suspend fun getCPInfos(packageName: String): List<Info> {
        val args = Bundle().let {
            it.putString("type", type.value)
            it.putString("packageName", packageName)
            it
        }

        val result = getCPResult(BasicApp.context, ProviderMethod.LoadInfos.value, args)

        return if (result != null) {
            try {
                val infos = result.getSerializable("infos") as Array<Info>?
                infos?.toList() ?: emptyList()
            } catch (e: Exception) {
                getCPResult(BasicApp.context, ProviderMethod.ClearAll.value, Bundle())
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun resumeSt2Info() {
        daDao.insert(
            daDao.loadStsDB(type).map {
                Info(
                    name = it.name, type = it.type,
                    packageName = it.packageName, userId = it.userId
                )
            })
    }

}