package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.utils.ResUtil

/**
 * 剧集项组件
 * 对应现有 BaseEpisodeHolder
 * 添加最大宽度限制（对应 Java 原版 maxWidth = ResUtil.getScreenWidth() - ResUtil.dp2px(32)）
 */
@Composable
fun EpisodeItem(
    episode: Episode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val maxWidth = ResUtil.getScreenWidth(context) - ResUtil.dp2px(32)

    Card(
        onClick = onClick,
        modifier = modifier.widthIn(max = maxWidth.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (episode.isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = episode.getDesc().toString() + episode.getName(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (episode.isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
