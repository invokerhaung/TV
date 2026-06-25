package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.utils.ResUtil

/**
 * 剧集列表对话框
 * 对应 EpisodeListDialog
 * 使用 ModalBottomSheet 实现（对应 Java 原版 BaseSideSheetDialog）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeListDialog(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()
    val selectedIndex = episodes.indexOfFirst { it.isSelected() }

    // 滚动到选中的位置（对应 Java 原版 binding.recycler.scrollToPosition）
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    // 计算最大宽度（对应 Java 原版 getWidth()）
    val maxWidth = ResUtil.getScreenWidth(context) / 3

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(episodes) { episode ->
                EpisodeListItem(
                    episode = episode,
                    onClick = {
                        onEpisodeClick(episode)
                        onDismiss()
                    }
                )
            }

            // 关闭按钮
            item {
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
}

@Composable
private fun EpisodeListItem(
    episode: Episode,
    onClick: () -> Unit
) {
    Text(
        text = episode.getDesc().toString() + episode.getName(),
        style = MaterialTheme.typography.bodyLarge,
        color = if (episode.isSelected()) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}
