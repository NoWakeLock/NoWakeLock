package com.js.nowakelock

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.gson.Gson
import com.js.nowakelock.data.broadcastreceiver.PowerConnectionReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BasicApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var gson: Gson
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        gson = Gson()

        //koin
        //startKoin {
        //    androidContext(this@BasicApp)
        //    modules(repository, viewModel)
        //}
        startKoin {
            androidContext(this@BasicApp)
            modules(appModule())
        }

        // for PowerConnectionReceiver
        registerPowerConnectionReceiver()
    }

    private fun registerPowerConnectionReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(PowerConnectionReceiver(), filter)
    }
}