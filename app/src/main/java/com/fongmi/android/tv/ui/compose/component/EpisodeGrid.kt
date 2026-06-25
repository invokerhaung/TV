package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.utils.ResUtil

/**
 * 剧集网格组件
 * 对应现有 EpisodeGridHolder
 */
@Composable
fun EpisodeGrid(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(episodes) { _, episode ->
            EpisodeItem(
                episode = episode,
                onClick = { onEpisodeClick(episode) }
            )
        }
    }
}

/**
 * 剧集列表组件（横向滚动）
 * 对应现有 EpisodeHoriHolder
 * 添加最大宽度限制（对应 Java 原版 maxWidth = ResUtil.getScreenWidth() - ResUtil.dp2px(32)）
 */
@Composable
fun EpisodeList(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val maxWidth = ResUtil.getScreenWidth(context) - ResUtil.dp2px(32)

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(episodes.size) { index ->
            val episode = episodes[index]
            EpisodeItem(
                episode = episode,
                onClick = { onEpisodeClick(episode) },
                modifier = Modifier.widthIn(max = maxWidth.dp)
            )
        }
    }
}
