package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Track

/**
 * 音轨对话框
 * 对应 TrackDialog
 * 支持文件选择器（对应 Java 原版 FileChooser）
 */
@Composable
fun TrackDialog(
    title: String,
    tracks: List<Track>,
    showOffset: Boolean = false,
    showChoose: Boolean = false,
    showSubtitle: Boolean = false,
    onTrackClick: (Track) -> Unit,
    onOffsetClick: () -> Unit,
    onChooseClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onDismiss: () -> Unit
) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (tracks.isEmpty()) {
                    Text(
                        text = "暂无可用轨道",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tracks) { track ->
                            TrackItem(
                                track = track,
                                onClick = {
                                    onTrackClick(track)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                // 功能按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showOffset) {
                        Button(
                            onClick = onOffsetClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("偏移")
                        }
                    }
                    if (showChoose) {
                        Button(
                            onClick = onChooseClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("选择文件")
                        }
                    }
                    if (showSubtitle) {
                        Button(
                            onClick = onSubtitleClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("字幕设置")
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

@Composable
private fun TrackItem(
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = track.getName(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (track.isSelected()) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
