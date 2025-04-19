package com.js.nowakelock

import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.repository.appdas.AppDasAR
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.data.repository.backup.BackupRepo
import com.js.nowakelock.data.repository.daitem.WakelockRepositoryImpl
import com.js.nowakelock.data.repository.daitem.AlarmRepositoryImpl
import com.js.nowakelock.data.repository.daitem.ServiceRepositoryImpl
import com.js.nowakelock.ui.screens.alarms.AlarmsViewModel
import com.js.nowakelock.ui.screens.services.ServicesViewModel
import com.js.nowakelock.ui.screens.settings.SettingsViewModel
import com.js.nowakelock.ui.screens.das.DAsViewModel
import com.js.nowakelock.ui.screens.apps.AppsViewModel
import com.js.nowakelock.data.repository.daDetail.DAInfoRepository
import com.js.nowakelock.data.repository.daDetail.DAInfoRepositoryImpl
import com.js.nowakelock.data.repository.daDetail.DADetailRepository
import com.js.nowakelock.data.repository.daDetail.DADetailRepositoryImpl
import com.js.nowakelock.ui.screens.dadetail.DADetailViewModel
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.viewModel

fun appModule() = module {

    //
    single { AppDatabase.getInstance(get()) }
    factory { get<AppDatabase>().appInfoDao() }
    factory { get<AppDatabase>().dADao() }
    factory { get<AppDatabase>().appDaDao() }
    factory { get<AppDatabase>().infoEventDao() }


    // Repository
    singleOf(::AppDasAR) { bind<AppDasRepo>() }
    singleOf(::BackupRepo)
    single { UserPreferencesRepository(get()) }

    single { WakelockRepositoryImpl(get(), get()) }
    single { AlarmRepositoryImpl(get(), get()) }
    single { ServiceRepositoryImpl(get(), get()) }

    //
    singleOf(::DAInfoRepositoryImpl) { bind<DAInfoRepository>() }
    singleOf(::DADetailRepositoryImpl) { bind<DADetailRepository>() }

    // ViewModel
    viewModel(named("WakelockViewModel")) {
        DAsViewModel(get<WakelockRepositoryImpl>())
    }

    viewModel(named("AlarmViewModel")) {
        DAsViewModel(get<AlarmRepositoryImpl>())
    }

    viewModel(named("ServiceViewModel")) {
        DAsViewModel(get<ServiceRepositoryImpl>())
    }

    viewModelOf(::AppsViewModel)
    viewModelOf(::AlarmsViewModel)
    viewModelOf(::ServicesViewModel)
    viewModelOf(::SettingsViewModel)

    viewModel {
        DADetailViewModel(
            savedStateHandle = get(),
            daDetailRepository = get<DADetailRepository>(),
            daInfoRepository = get<DAInfoRepository>()
        )
    }

    viewModel { SettingsViewModel(get()) }
}
