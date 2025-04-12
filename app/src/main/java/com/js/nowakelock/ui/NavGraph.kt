package com.js.nowakelock.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.js.nowakelock.ui.screens.dadetail.DADetailScreen
//import com.js.nowakelock.util.NavRoutes

///**
// * Add the DA detail screen to the navigation graph.
// *
// * @param onNavigateBack Callback for navigating back
// */
//fun NavGraphBuilder.daDetailScreen(
//    onNavigateBack: () -> Unit
//) {
//    composable(
//        route = "${NavRoutes.DA_DETAIL}/{${NavRoutes.ARG_DA_NAME}}/{${NavRoutes.ARG_DA_TYPE}}/{${NavRoutes.ARG_USER_ID}}",
//        arguments = listOf(
//            navArgument(NavRoutes.ARG_DA_NAME) { type = NavType.StringType },
//            navArgument(NavRoutes.ARG_DA_TYPE) { type = NavType.StringType },
//            navArgument(NavRoutes.ARG_USER_ID) { type = NavType.IntType; defaultValue = 0 }
//        )
//    ) { backStackEntry ->
//        val daId = backStackEntry.arguments?.getString(NavRoutes.ARG_DA_NAME) ?: ""
//        val type = backStackEntry.arguments?.getString(NavRoutes.ARG_DA_TYPE) ?: ""
//        val userId = backStackEntry.arguments?.getInt(NavRoutes.ARG_USER_ID) ?: 0
//
//        DADetailScreen(
//            daId = daId,
//            type = type,
//            userId = userId,
//            onNavigateBack = onNavigateBack
//        )
//    }
//}
//
///**
// * Navigate to the DA detail screen.
// *
// * @param daName The name of the device automation item
// * @param daType The type of the device automation item
// * @param userId The user ID
// * @param navOptions Optional navigation options
// */
//fun NavController.navigateToDADetail(
//    daName: String,
//    daType: String,
//    userId: Int = 0,
//    navOptions: NavOptions? = null
//) {
//    navigate("${NavRoutes.DA_DETAIL}/$daName/$daType/$userId", navOptions)
//}