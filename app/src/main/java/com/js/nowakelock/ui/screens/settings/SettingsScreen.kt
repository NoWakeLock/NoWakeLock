package com.js.nowakelock.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.R
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.ThemeMode
import com.js.nowakelock.ui.navigation.NavRoutes
import com.js.nowakelock.ui.screens.settings.components.SettingsCard
import com.js.nowakelock.ui.screens.settings.components.SettingsCategoryTitle
import com.js.nowakelock.ui.screens.settings.components.SettingsSelectableItem
import com.js.nowakelock.ui.screens.settings.components.SettingsSwitchItem
import com.js.nowakelock.ui.screens.settings.components.SettingsValueItem
import com.js.nowakelock.ui.screens.settings.components.SettingsActionItem
import com.js.nowakelock.ui.theme.NoWakeLockTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen that displays user preferences for the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // collect the state
    val themeMode by viewModel.themeMode.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val powerFlag by viewModel.powerFlag.collectAsState()
    val clearFlag by viewModel.clearFlag.collectAsState()
    val debugMode by viewModel.debugMode.collectAsState()

    // Dialog states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // file selection result handler
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.createBackup(it) }
    }
    
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it) }
    }

    // Show error message in snackbar if needed
    LaunchedEffect(uiState.message) {
        if (uiState.message.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // interface settings area - use remember to cache the unchanged part
                InterfaceSettings(
                    themeMode = themeMode,
                    languageMode = languageMode,
                    onShowThemeDialog = { showThemeDialog = true },
                    onShowLanguageDialog = { showLanguageDialog = true }
                )
                
                // data management settings area - encapsulated separately, only when related status changes will it be重组
                DataManagementSettings(
                    powerFlag = powerFlag,
                    clearFlag = clearFlag,
                    onPowerFlagChanged = { viewModel.updatePowerFlag(it) },
                    onClearFlagChanged = { viewModel.updateClearFlag(it) }
                )
                
                // backup and restore function
                BackupSettings(
                    isBackupInProgress = uiState.backupInProgress,
                    isRestoreInProgress = uiState.restoreInProgress,
                    onCreateBackup = {
                        createDocumentLauncher.launch("NoWakeLock-Backup-${viewModel.getFormattedDate()}.json")
                    },
                    onRestoreBackup = {
                        openDocumentLauncher.launch(arrayOf("application/json"))
                    }
                )
                
                // experimental function settings
                ExperimentalSettings(
                    debugMode = debugMode,
                    onDebugModeChanged = { viewModel.updateDebugMode(it) },
                    navController = navController
                )
            }
        }
    }

    // Theme selection dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { viewModel.updateTheme(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = languageMode,
            onLanguageSelected = { viewModel.updateLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

// interface settings component - use SettingsValueItem
@Composable
private fun InterfaceSettings(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    onShowThemeDialog: () -> Unit,
    onShowLanguageDialog: () -> Unit
) {
    SettingsCategoryTitle(title = stringResource(id = R.string.settings_interface))
    
    SettingsCard {
        // theme selection item
        SettingsValueItem(
            title = stringResource(id = R.string.theme),
            subtitle = stringResource(id = R.string.theme_list),
            value = getThemeSubtitle(themeMode),
            onClick = onShowThemeDialog
        )

        // language selection item
        SettingsValueItem(
            title = stringResource(id = R.string.language),
            subtitle = null,
            value = getLanguageSubtitle(languageMode),
            onClick = onShowLanguageDialog
        )
    }
}

// data management settings component - keep SettingsSwitchItem but use new implementation
@Composable
private fun DataManagementSettings(
    powerFlag: Boolean,
    clearFlag: Boolean,
    onPowerFlagChanged: (Boolean) -> Unit,
    onClearFlagChanged: (Boolean) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    SettingsCategoryTitle(title = stringResource(id = R.string.settings_data_management))
    
    SettingsCard {
        // Power Connection Toggle
        SettingsSwitchItem(
            title = stringResource(id = R.string.power_connection_detection),
            subtitle = stringResource(id = R.string.power_connection_detection_desc),
            checked = powerFlag,
            onCheckedChange = onPowerFlagChanged
        )
            
        // Clear Data Toggle - only available when powerFlag is enabled
        SettingsSwitchItem(
            title = stringResource(id = R.string.clear_inactive_data),
            subtitle = stringResource(id = R.string.clear_inactive_data_desc),
            checked = clearFlag,
            enabled = powerFlag,
            onCheckedChange = onClearFlagChanged
        )
    }
}

// backup settings component - use SettingsActionItem
@Composable
private fun BackupSettings(
    isBackupInProgress: Boolean,
    isRestoreInProgress: Boolean,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    SettingsCategoryTitle(title = stringResource(id = R.string.settings_backup))
    
    SettingsCard {
        // create backup operation
        SettingsActionItem(
            title = stringResource(id = R.string.create_backup),
            subtitle = stringResource(id = R.string.create_backup_desc),
            actionText = stringResource(id = R.string.create_backup),
            actionIcon = Icons.Default.Backup,
            isLoading = isBackupInProgress,
            enabled = !isBackupInProgress && !isRestoreInProgress,
            onClick = onCreateBackup
        )
        
        // restore backup operation
        SettingsActionItem(
            title = stringResource(id = R.string.restore_backup),
            subtitle = stringResource(id = R.string.restore_backup_desc),
            actionText = stringResource(id = R.string.restore_backup),
            actionIcon = Icons.Default.Restore,
            isLoading = isRestoreInProgress,
            enabled = !isBackupInProgress && !isRestoreInProgress,
            onClick = onRestoreBackup
        )
    }
}

// experimental function settings component
@Composable
private fun ExperimentalSettings(
    debugMode: Boolean,
    onDebugModeChanged: (Boolean) -> Unit,
    navController: NavController? = null
) {
    Spacer(modifier = Modifier.height(16.dp))
    SettingsCategoryTitle(title = stringResource(id = R.string.settings_experimental))
    
    SettingsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp)
            )
            Text(
                text = stringResource(id = R.string.experimental),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Debug Mode Toggle
        SettingsSwitchItem(
            title = stringResource(id = R.string.debug_mode),
            subtitle = stringResource(id = R.string.debug_mode_desc),
            checked = debugMode,
            onCheckedChange = onDebugModeChanged
        )
        // module check operation
        SettingsActionItem(
            title = stringResource(id = R.string.module_check),
            subtitle = stringResource(id = R.string.module_check_description),
            actionText = stringResource(id = R.string.create_backup),
            actionIcon = Icons.Default.CheckCircle,
            isLoading = false,
            enabled = true,
            onClick = {
                navController?.navigate(NavRoutes.MODULE_CHECK)
            }
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.theme),
                style = MaterialTheme.typography.titleLarge
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth(0.9f)  // limit the dialog width
            .padding(horizontal = 0.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 0.dp)
            ) {
                SettingsSelectableItem(
                    title = stringResource(id = R.string.light_theme_name),
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = {
                        onThemeSelected(ThemeMode.LIGHT)
                        onDismiss()
                    },
                    icon = Icons.Default.LightMode
                )

                SettingsSelectableItem(
                    title = stringResource(id = R.string.dark_theme_name),
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = {
                        onThemeSelected(ThemeMode.DARK)
                        onDismiss()
                    },
                    icon = Icons.Default.DarkMode
                )

                SettingsSelectableItem(
                    title = stringResource(id = R.string.system_theme_name),
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = {
                        onThemeSelected(ThemeMode.SYSTEM)
                        onDismiss()
                    },
                    icon = Icons.Default.PhoneAndroid
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 0.dp, end = 0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: LanguageMode,
    onLanguageSelected: (LanguageMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.language),
                style = MaterialTheme.typography.titleLarge
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth(0.9f)  // limit the dialog width
            .padding(horizontal = 0.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 0.dp)
            ) {
                SettingsSelectableItem(
                    title = stringResource(id = R.string.english_language_name),
                    selected = currentLanguage == LanguageMode.ENGLISH,
                    onClick = {
                        onLanguageSelected(LanguageMode.ENGLISH)
                        onDismiss()
                    },
                    icon = Icons.Default.Language
                )

                SettingsSelectableItem(
                    title = stringResource(id = R.string.chinese_language_name),
                    selected = currentLanguage == LanguageMode.CHINESE,
                    onClick = {
                        onLanguageSelected(LanguageMode.CHINESE)
                        onDismiss()
                    },
                    icon = Icons.Default.Language
                )

                SettingsSelectableItem(
                    title = stringResource(id = R.string.french_language_name),
                    selected = currentLanguage == LanguageMode.FRENCH,
                    onClick = {
                        onLanguageSelected(LanguageMode.FRENCH)
                        onDismiss()
                    },
                    icon = Icons.Default.Language
                )

                SettingsSelectableItem(
                    title = stringResource(id = R.string.system_language_name),
                    selected = currentLanguage == LanguageMode.SYSTEM,
                    onClick = {
                        onLanguageSelected(LanguageMode.SYSTEM)
                        onDismiss()
                    },
                    icon = Icons.Default.PhoneAndroid
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 0.dp, end = 0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
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

// ThemeSelectionDialog preview
@Composable
@Preview
fun ThemeSelectionDialogPreview() {
    NoWakeLockTheme {
        ThemeSelectionDialog(
            currentTheme = ThemeMode.LIGHT,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

// LanguageSelectionDialog preview
@Composable
@Preview
fun LanguageSelectionDialogPreview() {
    NoWakeLockTheme {
        LanguageSelectionDialog(
            currentLanguage = LanguageMode.ENGLISH,
            onLanguageSelected = {},
            onDismiss = {}
        )
    }
}

// BackupSettings preview
@Composable
@Preview
fun BackupSettingsPreview() {
    NoWakeLockTheme {
        BackupSettings(isBackupInProgress = false, isRestoreInProgress = false, onCreateBackup = {}, onRestoreBackup = {})
    }
}

// ExperimentalSettings preview
@Composable
@Preview
fun ExperimentalSettingsPreview() {
    NoWakeLockTheme {
        ExperimentalSettings(
            debugMode = false,
            onDebugModeChanged = {},
            navController = null
        )
    }
}