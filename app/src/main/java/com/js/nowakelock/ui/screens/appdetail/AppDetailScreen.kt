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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.js.nowakelock.BasicApp.Companion.context
import com.js.nowakelock.R
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.ui.components.StatisticCard
import org.koin.androidx.compose.koinViewModel
import  com.js.nowakelock.data.db.entity.AppInfo

/**
 * 应用详情页面
 */
@Composable
fun AppDetailScreen(
    packageName: String,
    userId: Int = 0,
    onNavigateBack: () -> Unit,
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
                        onToggleBlock = { viewModel.toggleAppBlockStatus() }
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
    onToggleBlock: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("App", "Wakelocks", "Alarms", "Services")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 应用信息头部
        AppInfoHeader(
            appWithStats = appInfo,
            isBlocked = isBlocked,
            onToggleBlock = onToggleBlock
        )

        // 统计摘要卡片
        StatisticsSummaryRow(appInfo = appInfo)

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
            1 -> WakelocksTabContent(appInfo)
            2 -> AlarmsTabContent(appInfo)
            3 -> ServicesTabContent(appInfo)
        }
    }
}

/**
 * 应用信息头部
 */
@Composable
fun AppInfoHeader(
    appWithStats: AppWithStats,
    isBlocked: Boolean,
    onToggleBlock: () -> Unit
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
 * 统计摘要行
 */
@Composable
fun StatisticsSummaryRow(appInfo: AppWithStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 唤醒锁统计
        StatisticSummaryItem(
            value = appInfo.wakelockCount.toString(),
            label = stringResource(R.string.WakeLock),
            modifier = Modifier.weight(1f)
        )

        // 闹钟统计
        StatisticSummaryItem(
            value = appInfo.alarmCount.toString(),
            label = stringResource(R.string.Alarm),
            modifier = Modifier.weight(1f)
        )

        // 服务统计
        StatisticSummaryItem(
            value = appInfo.serviceCount.toString(),
            label = stringResource(R.string.Service),
            modifier = Modifier.weight(1f)
        )

        // 总时间
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "5h",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "32m",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.total_time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

        // 唤醒锁统计
        StatisticCard(
            title = stringResource(R.string.wakelock_statistics),
            items = listOf(
                Pair(stringResource(R.string.total_wakelocks), appInfo.wakelockCount.toString()),
                Pair(
                    stringResource(R.string.blocked_wakelocks),
                    appInfo.wakelockBlockedCount.toString()
                ),
                Pair(stringResource(R.string.total_time), "5h 32m"),
                Pair(stringResource(R.string.saved_time), "1h 15m")
            )
        )

        // 闹钟统计
        StatisticCard(
            title = stringResource(R.string.alarm_statistics),
            items = listOf(
                Pair(stringResource(R.string.total_alarms), appInfo.alarmCount.toString()),
                Pair(stringResource(R.string.blocked_alarms), appInfo.alarmBlockedCount.toString())
            )
        )

        // 服务统计
        StatisticCard(
            title = stringResource(R.string.service_statistics),
            items = listOf(
                Pair(stringResource(R.string.total_services), appInfo.serviceCount.toString()),
                Pair(
                    stringResource(R.string.blocked_services),
                    appInfo.serviceBlockedCount.toString()
                )
            )
        )
    }
}

/**
 * Wakelocks标签页内容
 */
@Composable
fun WakelocksTabContent(appInfo: AppWithStats) {
    // 这里实现唤醒锁列表
    Text(
        text = "Wakelocks content",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

/**
 * Alarms标签页内容
 */
@Composable
fun AlarmsTabContent(appInfo: AppWithStats) {
    // 这里实现闹钟列表
    Text(
        text = "Alarms content",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

/**
 * Services标签页内容
 */
@Composable
fun ServicesTabContent(appInfo: AppWithStats) {
    // 这里实现服务列表
    Text(
        text = "Services content",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
    AppDetailContent(appInfo = appInfo, isBlocked = false, onToggleBlock = {})
}
            

