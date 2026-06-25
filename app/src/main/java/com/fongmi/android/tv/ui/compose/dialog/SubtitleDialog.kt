package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 字幕对话框
 * 对应 SubtitleDialog
 * 支持全屏模式适配（对应 Java 原版 isFull() 和 transparent()）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleDialog(
    isFullscreen: Boolean = false,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLarge: () -> Unit,
    onSmall: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    // 全屏模式下使用深色主题（对应 Java 原版 MDColor.WHITE）
    val buttonColor = if (isFullscreen) {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    } else {
        ButtonDefaults.buttonColors()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isFullscreen) Color.Black else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "字幕设置",
                style = MaterialTheme.typography.titleLarge,
                color = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 位置调整
            Text(
                text = "位置",
                style = MaterialTheme.typography.titleSmall,
                color = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUp,
                    modifier = Modifier.weight(1f),
                    colors = buttonColor
                ) {
                    Text("上移")
                }
                Button(
                    onClick = onDown,
                    modifier = Modifier.weight(1f),
                    colors = buttonColor
                ) {
                    Text("下移")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 字号调整
            Text(
                text = "字号",
                style = MaterialTheme.typography.titleSmall,
                color = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLarge,
                    modifier = Modifier.weight(1f),
                    colors = buttonColor
                ) {
                    Text("放大")
                }
                Button(
                    onClick = onSmall,
                    modifier = Modifier.weight(1f),
                    colors = buttonColor
                ) {
                    Text("缩小")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 重置
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColor
            ) {
                Text("重置")
            }

            // 关闭按钮
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    "关闭",
                    color = if (isFullscreen) Color.White else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
