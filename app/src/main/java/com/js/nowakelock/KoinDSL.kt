package com.js.nowakelock

import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.repository.appda.AppDaR
import com.js.nowakelock.data.repository.appda.AppDaRepo
import com.js.nowakelock.data.repository.appdas.AppDasAR
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.data.repository.backup.BackupRepo
import com.js.nowakelock.data.repository.da.DaR
import com.js.nowakelock.data.repository.da.DaRepo
import com.js.nowakelock.data.repository.das.FR
import com.js.nowakelock.data.repository.das.IAlarmR
import com.js.nowakelock.data.repository.das.IServiceR
import com.js.nowakelock.data.repository.das.IWakelockR
import com.js.nowakelock.data.repository.wakelock.WakelockRepository
import com.js.nowakelock.data.repository.wakelock.WakelockRepositoryImpl
import com.js.nowakelock.ui.screens.apps.AppsViewModel
import com.js.nowakelock.ui.screens.alarms.AlarmsViewModel
import com.js.nowakelock.ui.screens.services.ServicesViewModel
import com.js.nowakelock.ui.screens.settings.SettingsViewModel
import com.js.nowakelock.ui.screens.wakelocks.WakelocksViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.bind

fun appModule() = module {

    // Repository
    singleOf(::AppDasAR) { bind<AppDasRepo>() }
    singleOf(::AppDaR) { bind<AppDaRepo>() }
    singleOf(::IWakelockR) { bind<FR>(); named("WakelockR") }
    singleOf(::IAlarmR) { bind<FR>(); named("AlarmR") }
    singleOf(::IServiceR) { bind<FR>(); named("ServiceR") }
    singleOf(::DaR) { bind<DaRepo>() }
    singleOf(::BackupRepo)
    
    // Wakelock Repository
    singleOf(::WakelockRepositoryImpl) { bind<WakelockRepository>() }

    //
    single { AppDatabase.getInstance(get()) }
    factory { get<AppDatabase>().appInfoDao() }
    factory { get<AppDatabase>().dADao() }
    factory { get<AppDatabase>().appDaDao() }

    // ViewModel
    viewModelOf(::AppsViewModel)
    viewModelOf(::WakelocksViewModel)
    viewModelOf(::AlarmsViewModel)
    viewModelOf(::ServicesViewModel)
    viewModelOf(::SettingsViewModel)
}