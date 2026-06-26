package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R
import com.fongmi.android.tv.bean.Config

/**
 * 配置对话框
 * 对应 ConfigDialog
 * 支持 URL 自动补全和文件选择器
 */
@Composable
fun ConfigDialog(
    config: Config,
    type: Int = 0, // 0: vod, 1: live, 2: wall
    edit: Boolean = false,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onFileChoose: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(config.getName() ?: "") }
    var url by remember { mutableStateOf(config.getUrl() ?: "") }
    var append by remember { mutableStateOf(true) }

    // 使用字符串资源（对应 Java 原版 R.string.setting_vod 等）
    val title = when (type) {
        0 -> stringResource(R.string.setting_vod)
        1 -> stringResource(R.string.setting_live)
        2 -> stringResource(R.string.setting_wall)
        else -> stringResource(R.string.setting_vod)
    }

    /**
     * URL 自动补全（对应 Java 原版 detect 方法）
     * 输入 h 自动补全 http://
     * 输入 f 自动补全 file://
     * 输入 a 自动补全 assets://
     */
    fun detectUrl(s: String) {
        if (append && "h".equals(s, ignoreCase = true)) {
            append = false
            url = "http://"
        } else if (append && "f".equals(s, ignoreCase = true)) {
            append = false
            url = "file://"
        } else if (append && "a".equals(s, ignoreCase = true)) {
            append = false
            url = "assets://"
        } else if (s.length > 1) {
            append = false
        } else if (s.isEmpty()) {
            append = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                if (edit) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.dialog_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = { newUrl ->
                        url = newUrl
                        detectUrl(newUrl)
                    },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm(name, url) }
                    ),
                    trailingIcon = {
                        if (onFileChoose != null) {
                            IconButton(onClick = onFileChoose) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_folder),
                                    contentDescription = stringResource(R.string.dialog_choose)
                                )
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, url) }
            ) {
                Text(if (edit) stringResource(R.string.dialog_edit) else stringResource(R.string.dialog_positive))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dialog_negative))
            }
        }
    )
}
