package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R
import kotlin.math.roundToInt

/**
 * 缓冲设置对话框
 * 对应 BufferDialog
 */
@Composable
fun BufferDialog(
    currentBuffer: Int = 5,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var buffer by remember { mutableIntStateOf(currentBuffer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.player_buffer),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${buffer}MB",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = buffer.toFloat(),
                    onValueChange = { buffer = it.roundToInt() },
                    valueRange = 1f..50f,
                    steps = 49
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(buffer) }
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
