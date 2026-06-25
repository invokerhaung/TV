package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Parse

/**
 * 播放控制对话框
 * 对应 ControlDialog
 * 支持解析源列表和长按事件
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ControlDialog(
    speed: Float = 1.0f,
    decode: String = "",
    opening: String = "",
    ending: String = "",
    repeat: Boolean = false,
    scales: List<String> = emptyList(),
    currentScale: String = "",
    showTrack: Boolean = false,
    showTitle: Boolean = false,
    showDanmaku: Boolean = false,
    showParse: Boolean = false,
    parses: List<Parse> = emptyList(),
    onSpeedChange: (Float) -> Unit,
    onDecodeClick: () -> Unit,
    onOpeningClick: () -> Unit,
    onOpeningLongClick: (() -> Unit)? = null,
    onEndingClick: () -> Unit,
    onEndingLongClick: (() -> Unit)? = null,
    onRepeatClick: () -> Unit,
    onScaleClick: (String) -> Unit,
    onTimerClick: () -> Unit,
    onTextClick: () -> Unit,
    onAudioClick: () -> Unit,
    onVideoClick: () -> Unit,
    onTitleClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onPlayerLongClick: (() -> Unit)? = null,
    onDanmakuClick: () -> Unit,
    onParseClick: ((Parse) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var currentSpeed by remember { mutableFloatStateOf(speed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "播放控制",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 速度控制
                Text(
                    text = "速度: %.1fx".format(currentSpeed),
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value = currentSpeed,
                    onValueChange = { currentSpeed = it },
                    valueRange = 0.5f..2.0f,
                    steps = 15,
                    onValueChangeFinished = { onSpeedChange(currentSpeed) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 画面比例
                if (scales.isNotEmpty()) {
                    Text(
                        text = "画面比例",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        scales.forEach { scale ->
                            Button(
                                onClick = { onScaleClick(scale) },
                                colors = if (scale == currentScale) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(scale)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 解析源列表（对应 Java 原版 ParseAdapter）
                if (showParse && parses.isNotEmpty()) {
                    Text(
                        text = "解析源",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        parses.forEach { parse ->
                            Button(
                                onClick = { onParseClick?.invoke(parse) },
                                colors = if (parse.isSelected) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(parse.getName())
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 功能按钮
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 解码按钮
                    Button(onClick = onDecodeClick) {
                        Text(decode)
                    }
                    // 片头按钮（支持长按重置）
                    Button(
                        onClick = onOpeningClick,
                        modifier = Modifier.combinedClickable(
                            onClick = { onOpeningClick() },
                            onLongClick = { onOpeningLongClick?.invoke() }
                        )
                    ) {
                        Text("片头 $opening")
                    }
                    // 片尾按钮（支持长按重置）
                    Button(
                        onClick = onEndingClick,
                        modifier = Modifier.combinedClickable(
                            onClick = { onEndingClick() },
                            onLongClick = { onEndingLongClick?.invoke() }
                        )
                    ) {
                        Text("片尾 $ending")
                    }
                    // 循环按钮
                    Button(
                        onClick = onRepeatClick,
                        colors = if (repeat) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        Text("循环")
                    }
                    // 定时按钮
                    Button(onClick = onTimerClick) {
                        Text("定时")
                    }
                    // 播放器按钮（支持长按）
                    Button(
                        onClick = onPlayerClick,
                        modifier = Modifier.combinedClickable(
                            onClick = { onPlayerClick() },
                            onLongClick = { onPlayerLongClick?.invoke() }
                        )
                    ) {
                        Text("播放器")
                    }

                    if (showTrack) {
                        Button(onClick = onTextClick) {
                            Text("字幕")
                        }
                        Button(onClick = onAudioClick) {
                            Text("音频")
                        }
                        Button(onClick = onVideoClick) {
                            Text("视频")
                        }
                    }

                    if (showTitle) {
                        Button(onClick = onTitleClick) {
                            Text("标题")
                        }
                    }

                    if (showDanmaku) {
                        Button(onClick = onDanmakuClick) {
                            Text("弹幕")
                        }
                    }
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
