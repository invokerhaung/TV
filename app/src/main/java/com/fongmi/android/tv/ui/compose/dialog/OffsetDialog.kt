package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 偏移对话框
 * 对应 OffsetDialog
 * 使用 ModalBottomSheet 实现（对应 Java 原版 BaseBottomSheetDialog）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffsetDialog(
    type: Int = 0, // 0: 字幕, 1: 音频
    offset: Float = 0f,
    onOffsetChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentOffset by remember { mutableFloatStateOf(offset) }
    val sheetState = rememberModalBottomSheetState()

    val title = when (type) {
        0 -> "字幕偏移"
        1 -> "音频偏移"
        else -> "偏移"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "偏移: %.1f 秒".format(currentOffset),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = currentOffset,
                onValueChange = { currentOffset = it },
                onValueChangeFinished = { onOffsetChange(currentOffset) },
                valueRange = -10f..10f,
                steps = 200
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
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
                Text("关闭")
            }
        }
    }
}
