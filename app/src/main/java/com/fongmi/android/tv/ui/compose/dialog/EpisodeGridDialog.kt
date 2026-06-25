package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.utils.ResUtil

/**
 * 剧集网格对话框
 * 对应 EpisodeGridDialog
 * 使用 LazyVerticalGrid 实现（对应 Java 原版 ViewPager2 + TabLayout + Fragment）
 * 添加横竖屏适配（对应 Java 原版 ResUtil.isLand）
 */
@Composable
fun EpisodeGridDialog(
    episodes: List<Episode>,
    reverse: Boolean = false,
    onEpisodeClick: (Episode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // 计算每页显示数量和页数（对应 Java 原版 setSpanCount）
    val avgLength = if (episodes.isNotEmpty()) {
        episodes.map { it.getName().length }.average().toInt()
    } else {
        0
    }
    val spanCount = when {
        avgLength >= 12 -> 1
        avgLength >= 8 -> 2
        avgLength >= 4 -> 3
        avgLength >= 2 -> 4
        else -> 5
    }
    // 横竖屏适配（对应 Java 原版 ResUtil.isLand ? 5 : 10）
    val rowCount = if (ResUtil.isLand(context)) 5 else 10
    val itemCount = spanCount * rowCount
    val pageCount = (episodes.size + itemCount - 1) / itemCount

    // 生成标签（对应 Java 原版 setTitles）
    val titles = if (reverse) {
        (0 until pageCount).map { i ->
            val start = episodes.size - i * itemCount
            val end = maxOf(start - itemCount + 1, 1)
            "$start - $end"
        }
    } else {
        (0 until pageCount).map { i ->
            val start = i * itemCount + 1
            val end = minOf((i + 1) * itemCount, episodes.size)
            "$start - $end"
        }
    }

    // 找到当前选中的标签（对应 Java 原版 setCurrentPage）
    val selectedIndex = episodes.indexOfFirst { it.isSelected() }
    if (selectedIndex >= 0) {
        selectedTab = selectedIndex / itemCount
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "剧集选择",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 标签栏（对应 Java 原版 TabLayout）
                if (titles.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 16.dp
                    ) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                    }
                }

                // 剧集网格（对应 Java 原版 EpisodeFragment）
                val startIndex = selectedTab * itemCount
                val endIndex = minOf(startIndex + itemCount, episodes.size)
                val pageEpisodes = episodes.subList(startIndex, endIndex)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(spanCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pageEpisodes) { episode ->
                        FilterChip(
                            selected = episode.isSelected(),
                            onClick = { onEpisodeClick(episode) },
                            label = {
                                Text(
                                    text = episode.getDesc().toString() + episode.getName(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
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
