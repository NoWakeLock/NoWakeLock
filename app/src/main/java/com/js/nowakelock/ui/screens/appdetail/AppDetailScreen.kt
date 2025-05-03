package com.js.nowakelock.ui.screens.appdetail

import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.js.nowakelock.BasicApp.Companion.context
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.ui.navigation.DADetail
import org.koin.androidx.compose.koinViewModel
import com.js.nowakelock.data.db.entity.AppInfo
import androidx.compose.ui.graphics.vector.ImageVector
import com.js.nowakelock.data.db.entity.AppSt
import androidx.compose.material3.SwitchDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton

/**
 * App Detail Screen - Entry point composable for the app details
 */
@Composable
fun AppDetailScreen(
    packageName: String,
    userId: Int = 0,
    onNavigateBack: () -> Unit,
    navController: NavController? = null,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    viewModel: AppDetailViewModel = koinViewModel()
) {
    // Collect UI state with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error ?: stringResource(R.string.unknown_error),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            else -> {
                uiState.appInfo?.let { appInfo ->
                    AppDetailContent(
                        appInfo = appInfo,
                        isBlocked = uiState.isBlocked,
                        appSt = uiState.appSt,
                        onToggleBlock = { viewModel.toggleAppBlockStatus() },
                        onUpdateWakelockBlock = { viewModel.updateWakelockBlock(it) },
                        onUpdateAlarmBlock = { viewModel.updateAlarmBlock(it) },
                        onUpdateServiceBlock = { viewModel.updateServiceBlock(it) },
                        onAddWakelockPattern = { viewModel.addWakelockPattern(it) },
                        onRemoveWakelockPattern = { viewModel.removeWakelockPattern(it) },
                        onAddAlarmPattern = { viewModel.addAlarmPattern(it) },
                        onRemoveAlarmPattern = { viewModel.removeAlarmPattern(it) },
                        onAddServicePattern = { viewModel.addServicePattern(it) },
                        onRemoveServicePattern = { viewModel.removeServicePattern(it) },
                        validateRegexPattern = { viewModel.validateRegexPattern(it) },
                        navController = navController,
                        packageName = packageName,
                        userId = userId,
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }
}

/**
 * App Detail Content - Manages tabs and content area
 */
@Composable
fun AppDetailContent(
    appInfo: AppWithStats,
    isBlocked: Boolean,
    appSt: AppSt?,
    onToggleBlock: () -> Unit,
    onUpdateWakelockBlock: (Boolean) -> Unit,
    onUpdateAlarmBlock: (Boolean) -> Unit,
    onUpdateServiceBlock: (Boolean) -> Unit,
    onAddWakelockPattern: (String) -> Unit,
    onRemoveWakelockPattern: (String) -> Unit,
    onAddAlarmPattern: (String) -> Unit,
    onRemoveAlarmPattern: (String) -> Unit,
    onAddServicePattern: (String) -> Unit,
    onRemoveServicePattern: (String) -> Unit,
    validateRegexPattern: (String) -> Boolean,
    navController: NavController? = null,
    packageName: String,
    userId: Int,
    isSearchActive: Boolean = false,
    searchQuery: String = ""
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("App", "Wakelocks", "Alarms", "Services")

    // Track which tabs have been loaded for lazy loading
    val loadedTabs = remember { mutableStateOf(setOf(0)) }

    // when selectedTabIndex changes, immediately include the new tab index
    val currentLoadedTabs by remember {
        derivedStateOf {
            loadedTabs.value + selectedTabIndex
        }
    }

    // update the persistent loaded tab set, so it remains loaded on next recomposition
    LaunchedEffect(selectedTabIndex) {
        loadedTabs.value = loadedTabs.value + selectedTabIndex
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                appSt?.let { appSettings ->
                    AppTabContent(
                        appInfo = appInfo,
                        appSt = appSettings,
                        onUpdateWakelockBlock = onUpdateWakelockBlock,
                        onUpdateAlarmBlock = onUpdateAlarmBlock,
                        onUpdateServiceBlock = onUpdateServiceBlock,
                        onAddWakelockPattern = onAddWakelockPattern,
                        onRemoveWakelockPattern = onRemoveWakelockPattern,
                        onAddAlarmPattern = onAddAlarmPattern,
                        onRemoveAlarmPattern = onRemoveAlarmPattern,
                        onAddServicePattern = onAddServicePattern,
                        onRemoveServicePattern = onRemoveServicePattern,
                        validateRegexPattern = validateRegexPattern
                    )
                }
            }

            1 -> {
                if (1 in currentLoadedTabs) {
                    WakelocksTabContent(
                        appInfo = appInfo,
                        packageName = packageName,
                        userId = userId,
                        isSearchActive = selectedTabIndex == 1 && isSearchActive,
                        searchQuery = if (selectedTabIndex == 1) searchQuery else "",
                        onNavigateToDetail = { name, pkgName ->
                            navController?.navigate(
                                DADetail(
                                    daName = name,
                                    packageName = pkgName,
                                    userId = userId,
                                    type = Type.Wakelock.value
                                )
                            )
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            2 -> {
                if (2 in currentLoadedTabs) {
                    AlarmsTabContent(
                        appInfo = appInfo,
                        packageName = packageName,
                        userId = userId,
                        isSearchActive = selectedTabIndex == 2 && isSearchActive,
                        searchQuery = if (selectedTabIndex == 2) searchQuery else "",
                        onNavigateToDetail = { name, pkgName ->
                            navController?.navigate(
                                DADetail(
                                    daName = name,
                                    packageName = pkgName,
                                    userId = userId,
                                    type = Type.Alarm.value
                                )
                            )
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            3 -> {
                if (3 in currentLoadedTabs) {
                    ServicesTabContent(
                        appInfo = appInfo,
                        packageName = packageName,
                        userId = userId,
                        isSearchActive = selectedTabIndex == 3 && isSearchActive,
                        searchQuery = if (selectedTabIndex == 3) searchQuery else "",
                        onNavigateToDetail = { name, pkgName ->
                            navController?.navigate(
                                DADetail(
                                    daName = name,
                                    packageName = pkgName,
                                    userId = userId,
                                    type = Type.Service.value
                                )
                            )
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * App
 */
@Composable
fun AppInfoHeader(
    appWithStats: AppWithStats
) {
    // Get app icon using PackageManager
    val appIcon = remember {
        try {
            context.packageManager.getApplicationIcon(appWithStats.appInfo.packageName)
                .toBitmap()
                .asImageBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)) // Slightly rounded corners for icon
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.takeIf { appIcon == null }
                            ?: Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = "Icon for ${appWithStats.appInfo.label}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback if icon can't be loaded
                    Text(
                        text = appWithStats.appInfo.label.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            // app name and package name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = appWithStats.appInfo.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = appWithStats.appInfo.processName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (appWithStats.appInfo.system) {
                    Text(
                        text = stringResource(R.string.android),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * App Tab Content - Displays app information and settings
 */
@Composable
fun AppTabContent(
    appInfo: AppWithStats,
    appSt: AppSt,
    onUpdateWakelockBlock: (Boolean) -> Unit,
    onUpdateAlarmBlock: (Boolean) -> Unit,
    onUpdateServiceBlock: (Boolean) -> Unit,
    onAddWakelockPattern: (String) -> Unit,
    onRemoveWakelockPattern: (String) -> Unit,
    onAddAlarmPattern: (String) -> Unit,
    onRemoveAlarmPattern: (String) -> Unit,
    onAddServicePattern: (String) -> Unit,
    onRemoveServicePattern: (String) -> Unit,
    validateRegexPattern: (String) -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Card - Using ElevatedCard to match DADetailScreen style
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Card title inside the card - Using titleLarge with color onSurface and bottom padding 12dp
                Text(
                    text = stringResource(R.string.app_statistics),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Statistics content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Wakelock statistics
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Wakelock count row
                        StatisticRowWithIcon(
                            icon = Icons.Outlined.Lock,
                            leftLabel = stringResource(R.string.total_wakelocks),
                            leftValue = appInfo.wakelockCount.toString(),
                            rightLabel = stringResource(R.string.blocked_wakelocks),
                            rightValue = appInfo.wakelockBlockedCount.toString()
                        )

                        // Wakelock time row
                        StatisticRowWithIcon(
                            icon = null,
                            leftLabel = stringResource(R.string.total_time),
                            leftValue = appInfo.getFormattedTime(),
                            rightLabel = stringResource(R.string.saved_time),
                            rightValue = appInfo.getFormattedBlockedTime()
                        )
                    }

                    // Alarm statistics
                    StatisticRowWithIcon(
                        icon = Icons.Outlined.AccessTime,
                        leftLabel = stringResource(R.string.total_alarms),
                        leftValue = appInfo.alarmCount.toString(),
                        rightLabel = stringResource(R.string.blocked_alarms),
                        rightValue = appInfo.alarmBlockedCount.toString(),
                        useAlternateBackground = true
                    )

                    // Service statistics
                    StatisticRowWithIcon(
                        icon = Icons.Outlined.Build,
                        leftLabel = stringResource(R.string.total_services),
                        leftValue = appInfo.serviceCount.toString(),
                        rightLabel = stringResource(R.string.blocked_services),
                        rightValue = appInfo.serviceBlockedCount.toString(),
                        useAlternateBackground = true
                    )
                }
            }
        }

        // Settings Card - Using ElevatedCard to match DADetailScreen style
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Card title inside the card - Using titleLarge with color onSurface and bottom padding 12dp
                Text(
                    text = stringResource(R.string.app_settings),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Block Settings Section
                BlockSettingsSection(
                    appSt = appSt,
                    onUpdateWakelockBlock = onUpdateWakelockBlock,
                    onUpdateAlarmBlock = onUpdateAlarmBlock,
                    onUpdateServiceBlock = onUpdateServiceBlock
                )

                // Add divider between sections
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Pattern Settings Section
                PatternSettingsSection(
                    title = stringResource(R.string.wakelock_patterns),
                    patterns = appSt.rE_Wakelock,
                    onAddPattern = onAddWakelockPattern,
                    onRemovePattern = onRemoveWakelockPattern,
                    validateRegexPattern = validateRegexPattern
                )

                // Add divider between pattern sections
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                PatternSettingsSection(
                    title = stringResource(R.string.alarm_patterns),
                    patterns = appSt.rE_Alarm,
                    onAddPattern = onAddAlarmPattern,
                    onRemovePattern = onRemoveAlarmPattern,
                    validateRegexPattern = validateRegexPattern
                )

                // Add divider between pattern sections
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                PatternSettingsSection(
                    title = stringResource(R.string.service_patterns),
                    patterns = appSt.rE_Service,
                    onAddPattern = onAddServicePattern,
                    onRemovePattern = onRemoveServicePattern,
                    validateRegexPattern = validateRegexPattern
                )
            }
        }
    }
}

/**
 * Block Settings Section component
 * Displays global block toggles for wakelock, alarm, and service
 * Updated to match DASettingsCard style
 */
@Composable
private fun BlockSettingsSection(
    appSt: AppSt,
    onUpdateWakelockBlock: (Boolean) -> Unit,
    onUpdateAlarmBlock: (Boolean) -> Unit,
    onUpdateServiceBlock: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Wakelock Toggle with subtitle
        SettingToggleWithSubtitle(
            title = stringResource(R.string.allow_wakelocks),
            subtitle = stringResource(R.string.allow_wakelocks_description),
            checked = !appSt.wakelock,
            onCheckedChange = onUpdateWakelockBlock,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Alarm Toggle with subtitle
        SettingToggleWithSubtitle(
            title = stringResource(R.string.allow_alarms),
            subtitle = stringResource(R.string.allow_alarms_description),
            checked = !appSt.alarm,
            onCheckedChange = onUpdateAlarmBlock,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // Service Toggle with subtitle
        SettingToggleWithSubtitle(
            title = stringResource(R.string.allow_services),
            subtitle = stringResource(R.string.allow_services_description),
            checked = !appSt.service,
            onCheckedChange = onUpdateServiceBlock,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Setting Toggle with Subtitle component
 * Displays a toggle switch with title and subtitle, matching MD3 pattern
 * Updated to match DASettingsCard's SwitchItem component
 */
@Composable
private fun SettingToggleWithSubtitle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Title and subtitle column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            // Main title - using titleMedium instead of bodyLarge
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            // Descriptive subtitle - using bodySmall instead of bodyMedium
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // Switch control - no additional modifiers
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/**
 * Displays a statistic row with icon and two label-value pairs
 */
@Composable
private fun StatisticRowWithIcon(
    icon: ImageVector?,
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    useAlternateBackground: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (useAlternateBackground) Modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon area
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Data area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left part (label and value)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Label on left
                    Text(
                        text = leftLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Value on right
                    Text(
                        text = leftValue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                // Right part (label and value)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    // Label on left
                    Text(
                        text = rightLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Value on right
                    Text(
                        text = rightValue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rightLabel.contains("blocked", ignoreCase = true))
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

/**
 * Pattern Settings Section component
 * Displays and manages regex patterns for blocking
 * Updated to follow MD3 design style and match DADetailScreen components
 */
@Composable
private fun PatternSettingsSection(
    title: String,
    patterns: Set<String>,
    onAddPattern: (String) -> Unit,
    onRemovePattern: (String) -> Unit,
    validateRegexPattern: (String) -> Boolean
) {
    var showInput by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }
    var isValidPattern by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Section Title Row with Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Add Button
            IconButton(
                onClick = { showInput = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_pattern),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Pattern Chips display
        if (patterns.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                patterns.forEach { pattern ->
                    PatternChip(
                        pattern = pattern,
                        onRemove = { onRemovePattern(pattern) }
                    )
                }
            }
        } else if (!showInput) {
            // Empty state message
            Text(
                text = stringResource(R.string.add_pattern),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Pattern Input Form with animation
        AnimatedVisibility(
            visible = showInput,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = {
                            inputValue = it
                            isValidPattern = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.pattern)) },
                        isError = !isValidPattern,
                        supportingText = {
                            if (!isValidPattern) {
                                Text(
                                    text = stringResource(R.string.invalid_regex),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel button
                        TextButton(
                            onClick = {
                                inputValue = ""
                                showInput = false
                                isValidPattern = true
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        
                        // Add button
                        Button(
                            onClick = {
                                if (validateRegexPattern(inputValue)) {
                                    onAddPattern(inputValue)
                                    inputValue = ""
                                    showInput = false
                                } else {
                                    isValidPattern = false
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pattern Chip component
 * Displays a regex pattern with delete button
 * Updated with Material Design 3 styling to match DADetailScreen components
 */
@Composable
private fun PatternChip(
    pattern: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pattern,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 4.dp)
            )

            // Compact delete button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Wakelocks标签页内容
 */
@Composable
fun WakelocksTabContent(
    appInfo: AppWithStats,
    packageName: String,
    userId: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onNavigateToDetail: (String, String) -> Unit
) {
    // 使用WakelockScreen，传递packageName和userId进行过滤
    val viewModel = koinViewModel<com.js.nowakelock.ui.screens.das.DAsViewModel>(
        qualifier = org.koin.core.qualifier.named("WakelockViewModel")
    )

    // 设置应用过滤器
    LaunchedEffect(packageName, userId) {
        viewModel.setAppFilter(packageName, userId)
    }

    // 使用WakelockScreen组件，复用现有实现
    com.js.nowakelock.ui.screens.das.WakelockScreen(
        navigateToDADetail = onNavigateToDetail,
        viewModel = viewModel,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        packageName = packageName,
        userId = userId
    )
}

/**
 * Alarms标签页内容
 */
@Composable
fun AlarmsTabContent(
    appInfo: AppWithStats,
    packageName: String,
    userId: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onNavigateToDetail: (String, String) -> Unit
) {
    // 使用AlarmScreen，传递packageName和userId进行过滤
    val viewModel = koinViewModel<com.js.nowakelock.ui.screens.das.DAsViewModel>(
        qualifier = org.koin.core.qualifier.named("AlarmViewModel")
    )

    // 设置应用过滤器
    LaunchedEffect(packageName, userId) {
        viewModel.setAppFilter(packageName, userId)
    }

    // 使用AlarmScreen组件，复用现有实现
    com.js.nowakelock.ui.screens.das.AlarmScreen(
        navigateToDADetail = onNavigateToDetail,
        viewModel = viewModel,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        packageName = packageName,
        userId = userId
    )
}

/**
 * Services标签页内容
 */
@Composable
fun ServicesTabContent(
    appInfo: AppWithStats,
    packageName: String,
    userId: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onNavigateToDetail: (String, String) -> Unit
) {
    // 使用ServiceScreen，传递packageName和userId进行过滤
    val viewModel = koinViewModel<com.js.nowakelock.ui.screens.das.DAsViewModel>(
        qualifier = org.koin.core.qualifier.named("ServiceViewModel")
    )

    // 设置应用过滤器
    LaunchedEffect(packageName, userId) {
        viewModel.setAppFilter(packageName, userId)
    }

    // 使用ServiceScreen组件，复用现有实现
    com.js.nowakelock.ui.screens.das.ServiceScreen(
        navigateToDADetail = onNavigateToDetail,
        viewModel = viewModel,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        packageName = packageName,
        userId = userId
    )
}

// Preview AppDetailContent
@Preview
@Composable
fun AppDetailContentPreview() {
    val appInfo = AppWithStats(
        appInfo = AppInfo(
            packageName = "com.example.app",
            processName = "com.example.app",
            icon = 0,
            system = false,
            label = "Example App"
        ),
        wakelockCount = 37202,
        wakelockBlockedCount = 132,
        wakelockTime = 578820000, // 160h 47m in milliseconds
        alarmCount = 9040,
        alarmBlockedCount = 78,
        serviceCount = 156,
        serviceBlockedCount = 12
    )

    val appSt = AppSt(
        packageName = "com.example.app",
        userId = 0,
        wakelock = true,
        alarm = false,
        service = true,
        rE_Wakelock = setOf("MyWakeLock.*"),
        rE_Alarm = setOf("AlarmManager.*"),
        rE_Service = emptySet()
    )

    AppDetailContent(
        appInfo = appInfo,
        isBlocked = false,
        appSt = appSt,
        onToggleBlock = {},
        onUpdateWakelockBlock = {},
        onUpdateAlarmBlock = {},
        onUpdateServiceBlock = {},
        onAddWakelockPattern = {},
        onRemoveWakelockPattern = {},
        onAddAlarmPattern = {},
        onRemoveAlarmPattern = {},
        onAddServicePattern = {},
        onRemoveServicePattern = {},
        validateRegexPattern = { true },
        navController = null,
        packageName = "com.example.app",
        userId = 0
    )
}

// Preview AppTabContent only
@Preview
@Composable
fun AppTabContentPreview() {
    val appInfo = AppWithStats(
        appInfo = AppInfo(
            packageName = "com.example.app",
            processName = "com.example.app",
            icon = 0,
            system = false,
            label = "Example App"
        ),
        wakelockCount = 37202,
        wakelockBlockedCount = 132,
        wakelockTime = 578820000, // 160h 47m in milliseconds
        alarmCount = 9040,
        alarmBlockedCount = 78,
        serviceCount = 156,
        serviceBlockedCount = 12
    )

    val appSt = AppSt(
        packageName = "com.example.app",
        userId = 0,
        wakelock = true,
        alarm = false,
        service = true,
        rE_Wakelock = setOf("MyWakeLock.*"),
        rE_Alarm = setOf("AlarmManager.*"),
        rE_Service = emptySet()
    )

    MaterialTheme {
        AppTabContent(
            appInfo = appInfo,
            appSt = appSt,
            onUpdateWakelockBlock = {},
            onUpdateAlarmBlock = {},
            onUpdateServiceBlock = {},
            onAddWakelockPattern = {},
            onRemoveWakelockPattern = {},
            onAddAlarmPattern = {},
            onRemoveAlarmPattern = {},
            onAddServicePattern = {},
            onRemoveServicePattern = {},
            validateRegexPattern = { true }
        )
    }
}
            

