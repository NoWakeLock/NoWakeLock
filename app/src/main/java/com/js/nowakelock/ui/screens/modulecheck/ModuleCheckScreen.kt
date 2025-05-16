package com.js.nowakelock.ui.screens.modulecheck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.CheckStatus
import com.js.nowakelock.data.model.ModuleCheckResult
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for module check functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleCheckScreen(
    onBackClick: () -> Unit,
    viewModel: ModuleCheckViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.loading))
                    }
                }
                
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_checking_module),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.checkModuleStatus() }) {
                            Text(text = stringResource(R.string.refresh))
                        }
                    }
                }
                
                uiState.result != null -> {
                    ModuleCheckContent(
                        result = uiState.result!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Content for module check screen displaying the check results
 */
@Composable
fun ModuleCheckContent(
    result: ModuleCheckResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        // Overall status card
        OverallStatusCard(
            status = result.overallStatus,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Module status card
        ModuleStatusCard(
            isActive = result.moduleActive,
            version = result.moduleVersion,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hook status card
        HookStatusCard(
            hookStatus = result.hookStatus,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Config path card
        ConfigPathCard(
            isValid = result.configPathValid,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Card displaying overall module status
 */
@Composable
fun OverallStatusCard(
    status: CheckStatus,
    modifier: Modifier = Modifier
) {
    val (color, icon, title) = when (status) {
        CheckStatus.NORMAL -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
            stringResource(R.string.normal_status)
        )
        CheckStatus.WARNING -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Warning,
            stringResource(R.string.warning_status)
        )
        CheckStatus.ERROR -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Error,
            stringResource(R.string.error_status)
        )
    }
    
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.overall_status),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val description = when (status) {
                CheckStatus.NORMAL -> stringResource(R.string.status_normal_description)
                CheckStatus.WARNING -> stringResource(R.string.status_warning_description)
                CheckStatus.ERROR -> stringResource(R.string.status_error_description)
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Card displaying module activation status
 */
@Composable
fun ModuleStatusCard(
    isActive: Boolean,
    version: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.module_status),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val (icon, text, color) = if (isActive) {
                Triple(
                    Icons.Default.CheckCircle,
                    stringResource(R.string.module_active),
                    MaterialTheme.colorScheme.primary
                )
            } else {
                Triple(
                    Icons.Default.Cancel,
                    stringResource(R.string.module_inactive),
                    MaterialTheme.colorScheme.error
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = color,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            if (version != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.module_version, version),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (!isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.fix_module_inactive),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card displaying hook status for each type
 */
@Composable
fun HookStatusCard(
    hookStatus: Map<Type, Boolean>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.hook_status),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wakelock hook status
            HookStatusItem(
                type = Type.Wakelock,
                isWorking = hookStatus[Type.Wakelock] ?: false
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Alarm hook status
            HookStatusItem(
                type = Type.Alarm,
                isWorking = hookStatus[Type.Alarm] ?: false
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Service hook status
            HookStatusItem(
                type = Type.Service,
                isWorking = hookStatus[Type.Service] ?: false
            )
            
            // Show fix suggestion if any hook is not working
            if (hookStatus.values.any { !it }) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.fix_hook_not_working),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * List item displaying status of a specific hook type
 */
@Composable
fun HookStatusItem(
    type: Type,
    isWorking: Boolean
) {
    val typeName = when (type) {
        Type.Wakelock -> stringResource(R.string.wakelock_hook)
        Type.Alarm -> stringResource(R.string.alarm_hook)
        Type.Service -> stringResource(R.string.service_hook)
        else -> type.value
    }
    
    val (icon, text, color) = if (isWorking) {
        Triple(
            Icons.Default.CheckCircle,
            stringResource(R.string.hook_working),
            MaterialTheme.colorScheme.primary
        )
    } else {
        Triple(
            Icons.Default.Cancel,
            stringResource(R.string.hook_not_working),
            MaterialTheme.colorScheme.error
        )
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = typeName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Card displaying configuration path status
 */
@Composable
fun ConfigPathCard(
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.config_path),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val (icon, text, color) = if (isValid) {
                Triple(
                    Icons.Default.CheckCircle,
                    stringResource(R.string.config_path_valid),
                    MaterialTheme.colorScheme.primary
                )
            } else {
                Triple(
                    Icons.Default.Cancel,
                    stringResource(R.string.config_path_invalid),
                    MaterialTheme.colorScheme.error
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = color,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            if (!isValid) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.fix_config_path),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
} 