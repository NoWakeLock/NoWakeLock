package com.js.nowakelock.ui.screens.appdetail

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.js.nowakelock.BasicApp.Companion.context
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.ui.navigation.DADetail
import org.koin.androidx.compose.koinViewModel
import com.js.nowakelock.data.db.entity.AppInfo
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.derivedStateOf

/**
 * 应用详情页面
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
                        onToggleBlock = { viewModel.toggleAppBlockStatus() },
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
 * 应用详情内容区域
 */
@Composable
fun AppDetailContent(
    appInfo: AppWithStats,
    isBlocked: Boolean,
    onToggleBlock: () -> Unit,
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
            0 -> AppTabContent(appInfo)
            1 -> {
                if (1 in currentLoadedTabs) {
                    WakelocksTabContent(
                        appInfo = appInfo,
                        packageName = packageName,
                        userId = userId,
                        isSearchActive = if (selectedTabIndex == 1) isSearchActive else false,
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
                        isSearchActive = if (selectedTabIndex == 2) isSearchActive else false,
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
                        isSearchActive = if (selectedTabIndex == 3) isSearchActive else false,
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
 * AppTab
 */
@Composable
fun AppTabContent(appInfo: AppWithStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_statistics),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Count card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 唤醒锁统计部分
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 唤醒锁数量行
                    StatisticRowWithIcon(
                        icon = Icons.Outlined.Lock,
                        leftLabel = stringResource(R.string.total_wakelocks),
                        leftValue = appInfo.wakelockCount.toString(),
                        rightLabel = stringResource(R.string.blocked_wakelocks),
                        rightValue = appInfo.wakelockBlockedCount.toString()
                    )
                    
                    // 唤醒锁时间行
                    StatisticRowWithIcon(
                        icon = null,
                        leftLabel = stringResource(R.string.total_time),
                        leftValue = appInfo.getFormattedTime(),
                        rightLabel = stringResource(R.string.saved_time),
                        rightValue = appInfo.getFormattedBlockedTime()
                    )
                }
                
                // 闹钟统计部分
                StatisticRowWithIcon(
                    icon = Icons.Outlined.AccessTime,
                    leftLabel = stringResource(R.string.total_alarms),
                    leftValue = appInfo.alarmCount.toString(),
                    rightLabel = stringResource(R.string.blocked_alarms),
                    rightValue = appInfo.alarmBlockedCount.toString(),
                    useAlternateBackground = true
                )
                
                // 服务统计部分
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
}

/**
 * 带图标的统计行，显示一个图标和两组统计数据
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
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标区域
            Box(
                modifier = Modifier.width(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 数据区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧部分(标签和值)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // 标签在左
                    Text(
                        text = leftLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    
                    // 值在右
                    Text(
                        text = leftValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
                
                // 右侧部分(标签和值)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    // 标签在左
                    Text(
                        text = rightLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    
                    // 值在右
                    Text(
                        text = rightValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

/**
 * 水平布局的统计项组件，标签在左侧，值在右侧
 */
@Composable
private fun StatItemHorizontal(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 统计项组件，用于在统计卡片内显示标签和值
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
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
    AppDetailContent(
        appInfo = appInfo,
        isBlocked = false,
        onToggleBlock = {},
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
    
    MaterialTheme {
        AppTabContent(appInfo = appInfo)
    }
}
            

