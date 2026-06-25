package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * 信息对话框
 * 对应 InfoDialog
 * 支持复制功能（长按复制 URL 和 Header）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoDialog(
    title: String = "",
    url: String = "",
    header: String = "",
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "信息",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (url.isNotEmpty()) {
                    Text(
                        text = "URL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.combinedClickable(
                            onClick = { onShare(title) },
                            onLongClick = {
                                // 长按复制 URL（对应 Java 原版 onCopy）
                                clipboardManager.setText(AnnotatedString(url))
                            }
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (header.isNotEmpty()) {
                    Text(
                        text = "Headers",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = header,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                // 长按复制 Header（对应 Java 原版 onCopy）
                                clipboardManager.setText(AnnotatedString(header))
                            }
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onShare(title) }
            ) {
                Text("分享")
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
