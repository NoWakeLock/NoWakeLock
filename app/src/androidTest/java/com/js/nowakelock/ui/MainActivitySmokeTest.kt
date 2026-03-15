package com.js.nowakelock.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.js.nowakelock.MainActivity
import com.js.nowakelock.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launches_andBottomNavIsVisible() {
        val activity = composeRule.activity
        val appsLabel = activity.getString(R.string.Apps)

        composeRule.onNode(
            hasText(appsLabel) and hasClickAction()
        ).assertIsDisplayed()
    }

    @Test
    fun canNavigateToSettings() {
        val activity = composeRule.activity
        val settingsLabel = activity.getString(R.string.settings)

        composeRule.onNode(
            hasText(settingsLabel) and hasClickAction()
        ).performClick()

        val themeLabel = activity.getString(R.string.theme)
        composeRule.onNodeWithText(themeLabel).assertIsDisplayed()
    }
}
