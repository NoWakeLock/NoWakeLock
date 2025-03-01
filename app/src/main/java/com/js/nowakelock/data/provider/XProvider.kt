package com.js.nowakelock.data.provider

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.infoToBundle
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.xposedhook.XpUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun getURI(): Uri {
    return Settings.System.CONTENT_URI
}

enum class ProviderMethod(var value: String) {
    UpCount("UpCount"),
    UpBlockCount("GetExtends"),
    UpCountTime("UpCountTime"),
    LoadInfos("LoadInfos"),
    LoadInfo("LoadInfo"),
    ClearCount("ClearCount"),
    ClearAll("ClearAll"),
    CheckHookActive("CheckHookActive")
}

class XProvider(
    context: Context
) {
//    private val tag = "ProviderHandler"

    private var db: InfoDatabase = InfoDatabase.getInstance(context)
    private var dao: InfoDao = db.infoDao()

    companion object {
        @Volatile
        private var instance: XProvider? = null

        fun getInstance(context: Context): XProvider {
            if (instance == null) {
                instance = XProvider(context)
            }
            return instance!!
        }
    }

    fun getMethod(methodName: String, bundle: Bundle): Bundle? {
        return when (methodName) {
            ProviderMethod.UpCount.value -> upCount(bundle)
            ProviderMethod.UpBlockCount.value -> upBlockCount(bundle)
            ProviderMethod.UpCountTime.value -> upCountTime(bundle)
            ProviderMethod.LoadInfos.value -> loadInfos(bundle)
            ProviderMethod.LoadInfo.value -> loadInfo(bundle)
            ProviderMethod.ClearCount.value -> clearCount(bundle)
            ProviderMethod.ClearAll.value -> clearAll(bundle)
            ProviderMethod.CheckHookActive.value -> checkHookActive(bundle)
            "test" -> test(bundle)
            else -> null
        }
    }

    private fun test(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: String = bundle.getString("type") ?: ""
        val count: Int = bundle.getInt("count")

        val info = Info(
            name = name,
            type = stringToType(type),
            count = count
        )

        runBlocking {
            db.infoDao().insert(info)
            db.infoDao().upBlockCountPO(name, stringToType(type))
        }

        val tmp = Bundle()
        tmp.putString("name", name)
        return tmp
    }

    private fun upCount(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)

//        LogUtil.d(XpUtil.Tag, "upCount: $name, $type, $packageName, $userId")

        runBlocking(Dispatchers.IO) {
            val info = dao.loadInfo(name, type, userId)
            if (info != null) {
                dao.upCountPO(name, type, userId)
            } else {
                dao.insert(
                    Info(
                        name = name, type = type,
                        packageName = packageName, userId = userId,
                        count = 1
                    )
                )
            }
        }
        return Bundle()
    }

    private fun upBlockCount(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val info = dao.loadInfo(name, type, userId)
            if (info != null) {
                dao.upBlockCountPO(name, type, userId)
            } else {
                dao.insert(
                    Info(
                        name = name, type = type,
                        packageName = packageName, userId = userId,
                        blockCount = 1
                    )
                )
            }
        }

        return Bundle()
    }

    private fun upCountTime(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        val time = bundle.getLong("time")

        CoroutineScope(Dispatchers.IO).launch {
            val info = dao.loadInfo(name, type, userId)
            if (info != null) {
                dao.upCountTime(time, name, type, userId)
            } else {
                dao.insert(
                    Info(
                        name = name, type = type,
                        packageName = packageName, userId = userId,
                        countTime = time
                    )
                )
            }
        }

        return Bundle()
    }

    private fun loadInfos(bundle: Bundle): Bundle {
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        val infos: Array<Info> = runBlocking(Dispatchers.IO) {
            if (packageName == "" && type == Type.UnKnow)
                dao.loadInfos().toTypedArray()
            else if (packageName == "" && type != Type.UnKnow)
                dao.loadInfos(type).toTypedArray()
            else if (packageName != "" && type == Type.UnKnow)
                dao.loadInfos(packageName, userId).toTypedArray()
            else
                dao.loadInfos(packageName, type, userId).toTypedArray()
        }

        return Bundle().let {
            it.putSerializable("infos", infos)
            it
        }
    }

    private fun loadInfo(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")

        val info: Info = runBlocking {
            dao.loadInfo(name, type) ?: Info(name = name, type = type)
        }

        return infoToBundle(info)
    }

    private fun clearCount(bundle: Bundle): Bundle {
        runBlocking {
            dao.rstAllCount()
            dao.rstAllCountTime()
        }
        return Bundle()
    }

    private fun clearAll(bundle: Bundle): Bundle {
        runBlocking {
            dao.clearAll()
        }
        return Bundle()
    }

    private fun checkHookActive(bundle: Bundle): Bundle {
        return Bundle().apply {
            putBoolean("active", true)
            putString("version", BuildConfig.VERSION_NAME)
        }
    }
}