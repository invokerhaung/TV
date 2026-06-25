package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 弹幕设置对话框
 * 对应 DanmakuSettingDialog
 * 添加开关功能（对应 Java 原版 DanmakuSettingPanel 的 show/hide 开关）
 */
@Composable
fun DanmakuSettingDialog(
    alpha: Float = 1.0f,
    fontSize: Float = 1.0f,
    speed: Float = 1.0f,
    show: Boolean = true,
    onAlphaChange: (Float) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onShowToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var currentAlpha by remember { mutableFloatStateOf(alpha) }
    var currentFontSize by remember { mutableFloatStateOf(fontSize) }
    var currentSpeed by remember { mutableFloatStateOf(speed) }
    var currentShow by remember { mutableStateOf(show) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "弹幕设置",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 弹幕显示开关（对应 Java 原版 DanmakuSettingPanel 的 show 开关）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "显示弹幕",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = currentShow,
                        onCheckedChange = { newValue ->
                            currentShow = newValue
                            onShowToggle(newValue)
                        }
                    )
                }

                // 透明度
                Text(
                    text = "透明度: ${(currentAlpha * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentAlpha,
                    onValueChange = { currentAlpha = it },
                    onValueChangeFinished = { onAlphaChange(currentAlpha) },
                    valueRange = 0f..1f
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 字号
                Text(
                    text = "字号: ${(currentFontSize * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentFontSize,
                    onValueChange = { currentFontSize = it },
                    onValueChangeFinished = { onFontSizeChange(currentFontSize) },
                    valueRange = 0.5f..2.0f
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 速度
                Text(
                    text = "速度: ${(currentSpeed * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentSpeed,
                    onValueChange = { currentSpeed = it },
                    onValueChangeFinished = { onSpeedChange(currentSpeed) },
                    valueRange = 0.5f..2.0f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        }
    )
}
