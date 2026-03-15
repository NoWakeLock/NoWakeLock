package com.js.nowakelock.ui.screens.das.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DAListItemControlSectionLayoutTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun timeoutField_isCompletelyDisplayed_onNarrowWidth() {
        val item = DAItem(
            name = "TEST_WAKELOCK",
            packageName = "com.example",
            type = Type.Wakelock,
            fullBlocked = false,
            screenOffBlock = false,
            timeWindowSec = 60
        )

        composeRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.width(300.dp)) {
                    DAListItem(
                        daItem = item,
                        onToggleFullBlock = {},
                        onToggleScreenOffBlock = {},
                        onTimeWindowChange = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("da_control_timeout_field", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun timeoutRow_wraps_belowAllowRow_onVeryNarrowWidth() {
        val item = DAItem(
            name = "TEST_WAKELOCK",
            packageName = "com.example",
            type = Type.Wakelock,
            fullBlocked = false,
            screenOffBlock = false,
            timeWindowSec = 60
        )

        composeRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.width(260.dp)) {
                    DAListItem(
                        daItem = item,
                        onToggleFullBlock = {},
                        onToggleScreenOffBlock = {},
                        onTimeWindowChange = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()

        val allowTop = composeRule
            .onNodeWithTag("da_control_allow", useUnmergedTree = true)
            .getBoundsInRoot()
            .top
        val timeoutTop = composeRule
            .onNodeWithTag("da_control_timeout", useUnmergedTree = true)
            .getBoundsInRoot()
            .top

        assertTrue(
            "Expected timeout control row to wrap below allow row on narrow width",
            timeoutTop > allowTop
        )
    }
}

