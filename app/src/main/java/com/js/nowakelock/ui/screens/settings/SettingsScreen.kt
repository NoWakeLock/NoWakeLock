package com.js.nowakelock.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.ThemeMode
import com.js.nowakelock.ui.screens.settings.components.SettingsCard
import com.js.nowakelock.ui.screens.settings.components.SettingsCategoryTitle
import com.js.nowakelock.ui.screens.settings.components.SettingsDialogTitle
import com.js.nowakelock.ui.screens.settings.components.SettingsSelectableItem
import com.js.nowakelock.ui.theme.NoWakeLockTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen that displays user preferences for the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()

    // Dialog states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in snackbar if needed
    LaunchedEffect(uiState.message) {
        if (uiState.message.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Interface settings section
                SettingsCategoryTitle(title = stringResource(id = R.string.settings_interface))

                // Theme settings
                SettingsCard {
                    Column {
                        // Theme selector
                        SettingsSelectableItem(
                            title = stringResource(id = R.string.theme),
                            subtitle = getThemeSubtitle(themeMode),
                            selected = false,
                            onClick = { showThemeDialog = true }
                        )

                        // Language selector
                        SettingsSelectableItem(
                            title = stringResource(id = R.string.language),
                            subtitle = getLanguageSubtitle(languageMode),
                            selected = false,
                            onClick = { showLanguageDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Theme selection dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { SettingsDialogTitle(title = stringResource(id = R.string.theme)) },
            text = {
                Column {
                    SettingsSelectableItem(
                        title = stringResource(id = R.string.light_theme_name),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = {
                            viewModel.updateTheme(ThemeMode.LIGHT)
                            showThemeDialog = false
                        },
                        icon = Icons.Default.LightMode
                    )

                    SettingsSelectableItem(
                        title = stringResource(id = R.string.dark_theme_name),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = {
                            viewModel.updateTheme(ThemeMode.DARK)
                            showThemeDialog = false
                        },
                        icon = Icons.Default.DarkMode
                    )

                    SettingsSelectableItem(
                        title = stringResource(id = R.string.system_theme_name),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = {
                            viewModel.updateTheme(ThemeMode.SYSTEM)
                            showThemeDialog = false
                        },
                        icon = Icons.Default.PhoneAndroid
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { SettingsDialogTitle(title = stringResource(id = R.string.language)) },
            text = {
                Column {
                    SettingsSelectableItem(
                        title = stringResource(id = R.string.english_language_name),
                        selected = languageMode == LanguageMode.ENGLISH,
                        onClick = {
                            viewModel.updateLanguage(LanguageMode.ENGLISH)
                            showLanguageDialog = false
                        },
                        icon = Icons.Default.Language
                    )

                    SettingsSelectableItem(
                        title = stringResource(id = R.string.chinese_language_name),
                        selected = languageMode == LanguageMode.CHINESE,
                        onClick = {
                            viewModel.updateLanguage(LanguageMode.CHINESE)
                            showLanguageDialog = false
                        },
                        icon = Icons.Default.Language
                    )

                    SettingsSelectableItem(
                        title = stringResource(id = R.string.french_language_name),
                        selected = languageMode == LanguageMode.FRENCH,
                        onClick = {
                            viewModel.updateLanguage(LanguageMode.FRENCH)
                            showLanguageDialog = false
                        },
                        icon = Icons.Default.Language
                    )

                    SettingsSelectableItem(
                        title = stringResource(id = R.string.system_language_name),
                        selected = languageMode == LanguageMode.SYSTEM,
                        onClick = {
                            viewModel.updateLanguage(LanguageMode.SYSTEM)
                            showLanguageDialog = false
                        },
                        icon = Icons.Default.PhoneAndroid
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

/**
 * Get human-readable subtitle for theme mode
 */
@Composable
private fun getThemeSubtitle(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> stringResource(id = R.string.light_theme_name)
        ThemeMode.DARK -> stringResource(id = R.string.dark_theme_name)
        ThemeMode.SYSTEM -> stringResource(id = R.string.system_theme_name)
    }
}

/**
 * Get human-readable subtitle for language mode
 */
@Composable
private fun getLanguageSubtitle(languageMode: LanguageMode): String {
    return when (languageMode) {
        LanguageMode.ENGLISH -> stringResource(id = R.string.english_language_name)
        LanguageMode.CHINESE -> stringResource(id = R.string.chinese_language_name)
        LanguageMode.FRENCH -> stringResource(id = R.string.french_language_name)
        LanguageMode.SYSTEM -> stringResource(id = R.string.system_language_name)
    }
}