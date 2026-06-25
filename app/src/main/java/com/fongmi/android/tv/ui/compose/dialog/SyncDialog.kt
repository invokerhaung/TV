package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R
import com.fongmi.android.tv.bean.Device
import com.fongmi.android.tv.setting.Setting

/**
 * 同步对话框
 * 对应 SyncDialog
 * 支持同步模式切换和长按强制同步
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SyncDialog(
    devices: List<Device>,
    type: String = "history",
    onDeviceClick: (Device, Int) -> Unit,
    onDeviceLongClick: ((Device, Int) -> Unit)? = null,
    onRefresh: () -> Unit,
    onScan: () -> Unit,
    onDismiss: () -> Unit
) {
    // 同步模式（对应 Java 原版 Setting.getSyncMode()）
    var syncMode by remember { mutableIntStateOf(Setting.getSyncMode()) }

    // 模式图标资源（对应 Java 原版 R.array.cast_mode）
    val modeIcons = listOf(
        R.drawable.ic_action_sync,
        R.drawable.ic_action_sync,
        R.drawable.ic_action_sync
    )

    /**
     * 切换同步模式（对应 Java 原版 onMode()）
     */
    fun toggleMode() {
        syncMode = if (syncMode >= modeIcons.size - 1) 0 else syncMode + 1
        Setting.putSyncMode(syncMode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "同步${if (type == "history") "历史" else "收藏"}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row {
                    // 同步模式切换按钮（对应 Java 原版 binding.mode）
                    IconButton(onClick = { toggleMode() }) {
                        Icon(
                            painter = painterResource(id = modeIcons[syncMode]),
                            contentDescription = "同步模式: $syncMode",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // 刷新按钮
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 同步模式说明
                Text(
                    text = when (syncMode) {
                        0 -> "模式: 覆盖同步"
                        1 -> "模式: 强制同步"
                        2 -> "模式: 删除本地"
                        else -> "模式: 覆盖同步"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (devices.isEmpty()) {
                    Text(
                        text = "正在搜索设备...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(devices) { device ->
                            DeviceItem(
                                device = device,
                                onClick = { onDeviceClick(device, syncMode) },
                                onLongClick = {
                                    // 长按强制同步（对应 Java 原版 onLongClick）
                                    // mode 0: 不支持长按
                                    // mode 1: 强制同步（添加 &force=true）
                                    // mode 2: 删除本地后同步
                                    if (syncMode != 0) {
                                        onDeviceLongClick?.invoke(device, syncMode)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onScan
            ) {
                Text("扫描")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeviceItem(
    device: Device,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = device.getName(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = device.getIp(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
