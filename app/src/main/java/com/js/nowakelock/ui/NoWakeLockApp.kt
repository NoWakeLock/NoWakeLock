package com.js.nowakelock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.ui.components.NoWakeLockBottomNavBar
import com.js.nowakelock.ui.components.NoWakeLockTopAppBar
import com.js.nowakelock.ui.navigation.NoWakeLockNavGraph
import com.js.nowakelock.ui.theme.NoWakeLockTheme
import org.koin.androidx.compose.KoinAndroidContext

@Composable
fun NoWakeLockApp() {
    KoinAndroidContext{
        NoWakeLockTheme {
            val navController = rememberNavController()
            Scaffold(
                topBar = { NoWakeLockTopAppBar(navController) },
                bottomBar = { NoWakeLockBottomNavBar(navController) }
            ) { paddingValues ->
                NoWakeLockNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
@Preview
fun NoWakeLockAppPreview() {
    NoWakeLockApp()
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoWakeLockTheme {
        Greeting("Android")
    }
}