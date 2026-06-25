package com.fongmi.android.tv.ui.compose.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 更新对话框
 * 对应 UpdateDialog
 * 支持进度更新和不可取消性（setCancelable(false)）
 */
@Composable
fun UpdateDialog(
    title: String,
    desc: String,
    progress: Int = -1,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    // 阻止返回键关闭（对应 Java 原版 setCancelable(false)）
    BackHandler(enabled = true) {
        // 不执行任何操作，阻止返回
    }

    AlertDialog(
        onDismissRequest = {
            // 不允许点击外部关闭（对应 Java 原版 setCancelable(false)）
            // onDismiss 回调不被调用
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(
                    text = if (progress >= 0) "${progress}%" else "更新"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("取消")
            }
        }
    )
}
