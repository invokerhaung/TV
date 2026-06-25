package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Device

/**
 * 投屏对话框
 * 对应 CastDialog
 */
@Composable
fun CastDialog(
    devices: List<Device>,
    showScan: Boolean = false,
    onDeviceClick: (Device) -> Unit,
    onRefresh: () -> Unit,
    onScan: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "投屏",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                onClick = { onDeviceClick(device) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showScan) {
                TextButton(
                    onClick = onScan
                ) {
                    Text("扫描")
                }
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

@Composable
private fun DeviceItem(
    device: Device,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
