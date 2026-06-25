package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 定时器对话框
 * 对应 TimerDialog
 */
@Composable
fun TimerDialog(
    isRunning: Boolean = false,
    tick: Long = 0,
    onTimerSet: (Int) -> Unit,
    onDelay: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "定时器",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRunning) {
                    Text(
                        text = formatTime(tick),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Button(
                        onClick = onDelay,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("延迟")
                    }
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重置")
                    }
                } else {
                    TimerOption("15 分钟", 15, onTimerSet)
                    TimerOption("30 分钟", 30, onTimerSet)
                    TimerOption("45 分钟", 45, onTimerSet)
                    TimerOption("60 分钟", 60, onTimerSet)
                }
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

@Composable
private fun TimerOption(
    text: String,
    minutes: Int,
    onTimerSet: (Int) -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTimerSet(minutes) }
            .padding(vertical = 12.dp)
    )
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
