package com.js.nowakelock.ui.screens.appdetail

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
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
import com.js.nowakelock.ui.components.StatisticCard
import com.js.nowakelock.ui.navigation.DADetail
import org.koin.androidx.compose.koinViewModel
import com.js.nowakelock.data.db.entity.AppInfo

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
    val loadedTabs = remember { mutableSetOf(0) }

    // When tab changes, add it to loaded tabs
    LaunchedEffect(selectedTabIndex) {
        loadedTabs.add(selectedTabIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 标签页
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

        // 标签页内容
        when (selectedTabIndex) {
            0 -> AppTabContent(appInfo)
            1 -> {
                // Only show content if this tab has been loaded
                if (1 in loadedTabs) {
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
                    // Show loading placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            2 -> {
                // Only show content if this tab has been loaded
                if (2 in loadedTabs) {
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
                    // Show loading placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            3 -> {
                // Only show content if this tab has been loaded
                if (3 in loadedTabs) {
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
                    // Show loading placeholder
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
 * 统计摘要项
 */
@Composable
fun StatisticSummaryItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * App标签页内容
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

        // 合并统计卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 唤醒锁统计部分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 第一列：唤醒锁总数和总时间
                    Column(modifier = Modifier.weight(1f)) {
                        StatItemHorizontal(
                            label = stringResource(R.string.total_wakelocks),
                            value = appInfo.wakelockCount.toString()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatItemHorizontal(
                            label = stringResource(R.string.total_time),
                            value = appInfo.getFormattedTime()
                        )
                    }
                    
                    // 第二列：已屏蔽唤醒锁和节省时间
                    Column(modifier = Modifier.weight(1f)) {
                        StatItemHorizontal(
                            label = stringResource(R.string.blocked_wakelocks),
                            value = appInfo.wakelockBlockedCount.toString()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatItemHorizontal(
                            label = stringResource(R.string.saved_time),
                            value = "1h 15m"
                        )
                    }
                }
                
                // 分隔线
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // 闹钟统计部分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItemHorizontal(
                        label = stringResource(R.string.total_alarms),
                        value = appInfo.alarmCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatItemHorizontal(
                        label = stringResource(R.string.blocked_alarms),
                        value = appInfo.alarmBlockedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 分隔线
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // 服务统计部分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItemHorizontal(
                        label = stringResource(R.string.total_services),
                        value = appInfo.serviceCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatItemHorizontal(
                        label = stringResource(R.string.blocked_services),
                        value = appInfo.serviceBlockedCount.toString(),
                        modifier = Modifier.weight(1f)
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
            style = MaterialTheme.typography.bodyLarge,
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
            system = false
        ),
        wakelockCount = 10,
        wakelockBlockedCount = 2,
        alarmCount = 5,
        alarmBlockedCount = 1,
        serviceCount = 3,
        serviceBlockedCount = 0
    )
    AppDetailContent(
        appInfo = appInfo,
        isBlocked = false,
        onToggleBlock = {},
        navController = null,
        packageName = "",
        userId = 0
    )
}
            

