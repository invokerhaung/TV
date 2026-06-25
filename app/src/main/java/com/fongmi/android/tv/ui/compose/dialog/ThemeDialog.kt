package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R

/**
 * 主题颜色对话框
 * 对应 ThemeDialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeDialog(
    currentColor: Int = -1,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        -1 to "默认",
        0 to "动态",
        0xFF6750A4.toInt() to "紫色",
        0xFF3949AB.toInt() to "靛蓝",
        0xFF1E88E5.toInt() to "蓝色",
        0xFF00ACC1.toInt() to "青色",
        0xFF00897B.toInt() to "蓝绿",
        0xFF43A047.toInt() to "绿色",
        0xFF7CB342.toInt() to "浅绿",
        0xFFFB8C00.toInt() to "橙色",
        0xFFE53935.toInt() to "红色",
        0xFFD81B60.toInt() to "粉色",
        0xFF8E24AA.toInt() to "深紫",
        0xFF6D4C41.toInt() to "棕色"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.setting_theme_color),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colors.forEach { (colorValue, name) ->
                    val color = if (colorValue == -1 || colorValue == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color(colorValue)
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                onColorSelected(colorValue)
                            }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dialog_negative))
            }
        }
    )
}
