package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Live

/**
 * 直播源对话框
 * 对应 LiveDialog
 * 支持 setAction 逻辑（根据是否为全屏设置不同行为）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveDialog(
    lives: List<Live>,
    action: Boolean = false, // 对应 Java 原版 adapter.setAction(!isFull())
    onLiveClick: (Live) -> Unit,
    onBootToggle: (Int, Live) -> Unit,
    onPassToggle: (Int, Live) -> Unit,
    onBootLongClick: ((List<Live>) -> Unit)? = null,
    onPassLongClick: ((List<Live>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "直播源",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(lives) { index, live ->
                    LiveItem(
                        live = live,
                        action = action,
                        onClick = { onLiveClick(live) },
                        onBootToggle = { onBootToggle(index, live) },
                        onPassToggle = { onPassToggle(index, live) },
                        onBootLongClick = {
                            // 长按 boot 图标：全选/全取消所有直播源的 boot 属性
                            // 对应 Java 原版 onBootLongClick
                            onBootLongClick?.invoke(lives)
                        },
                        onPassLongClick = {
                            // 长按 pass 图标：全选/全取消所有直播源的 pass 属性
                            // 对应 Java 原版 onPassLongClick
                            onPassLongClick?.invoke(lives)
                        }
                    )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LiveItem(
    live: Live,
    action: Boolean,
    onClick: () -> Unit,
    onBootToggle: () -> Unit,
    onPassToggle: () -> Unit,
    onBootLongClick: () -> Unit,
    onPassLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = live.getName(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        if (action) {
            Checkbox(
                checked = live.isBoot(),
                onCheckedChange = { onBootToggle() },
                modifier = Modifier.combinedClickable(
                    onClick = { onBootToggle() },
                    onLongClick = { onBootLongClick() }
                )
            )
            Checkbox(
                checked = live.isPass(),
                onCheckedChange = { onPassToggle() },
                modifier = Modifier.combinedClickable(
                    onClick = { onPassToggle() },
                    onLongClick = { onPassLongClick() }
                )
            )
        }
    }
}
