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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

var repository = module {

    /** AppRepo */
    single<AppDasRepo>(named("AppDasR")) {
        AppDasAR(
            AppDatabase.getInstance(BasicApp.context).appInfoDao(),
            AppDatabase.getInstance(BasicApp.context).dADao(),
        )
    }

    single<AppDaRepo>(named("AppDaR")) {
        AppDaR(
            AppDatabase.getInstance(BasicApp.context).appDaDao()
        )
    }

    single<FR>(named("WakelockR")) {
        IWakelockR(AppDatabase.getInstance(BasicApp.context).dADao())
    }

    single<FR>(named("AlarmR")) {
        IAlarmR(AppDatabase.getInstance(BasicApp.context).dADao())
    }

    single<FR>(named("ServiceR")) {
        IServiceR(AppDatabase.getInstance(BasicApp.context).dADao())
    }

    single<DaRepo>(named("DaR")) {
        DaR(AppDatabase.getInstance(BasicApp.context).dADao())
    }

    single<BackupRepo>(named("BackupR")) {
        BackupRepo(
            AppDatabase.getInstance(BasicApp.context).appDaDao(),
            AppDatabase.getInstance(BasicApp.context).dADao()
        )
    }
}

var viewModel = module {

//    viewModel(named("FVm")) { (packageName: String, userId: Int, type: Type) ->
//        FBaseViewModel(
//            packageName, userId, when (type) {
//                Type.Wakelock -> get(named("WakelockR"))
//                Type.Alarm -> get(named("AlarmR"))
//                Type.Service -> get(named("ServiceR"))
//                else -> get(named("WakelockR"))
//            }
//        )
//    }

    // MainViewModel
//    viewModel(named("MainVm")) {
//        MainViewModel()
//    }
//
//    viewModel(named("AppDaSVM")) {
//        AppDaSViewModel(get(named("AppDasR")))
//    }
//
//    viewModel(named("DaVm")) { (name: String, type: Type, userId: Int) ->
//        DaViewModel(name, type, userId)
//    }
//
//    viewModel(named("AppDaVm")) { (packageName: String, userId: Int) ->
//        AppDaViewModel(packageName, userId)
//    }
//
//    viewModel(named("SettingVm")) {
//        SettingViewModel(get(named("BackupR")))
//    }

}