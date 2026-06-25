package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R

/**
 * UA 设置对话框
 * 对应 UaDialog
 * 支持 UA 自动补全（输入 c 补全 Chrome UA，输入 o 补全 OkHttp UA）
 */
@Composable
fun UaDialog(
    currentUa: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var ua by remember { mutableStateOf(currentUa) }
    var append by remember { mutableStateOf(true) }

    /**
     * UA 自动补全（对应 Java 原版 detect 方法）
     * 输入 c 自动补全 Chrome UA
     * 输入 o 自动补全 OkHttp UA
     */
    fun detectUa(s: String) {
        if (append && "c".equals(s, ignoreCase = true)) {
            append = false
            ua = com.github.catvod.utils.Util.CHROME
        } else if (append && "o".equals(s, ignoreCase = true)) {
            append = false
            ua = com.github.catvod.utils.Util.OKHTTP
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
                text = stringResource(R.string.player_ua),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ua,
                    onValueChange = { newUa ->
                        ua = newUa
                        detectUa(newUa)
                    },
                    label = { Text("UA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm(ua) }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(ua) }
            ) {
                Text(stringResource(R.string.dialog_positive))
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
