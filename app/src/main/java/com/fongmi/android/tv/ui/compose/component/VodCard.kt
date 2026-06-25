package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Vod

/**
 * 内容卡片样式
 */
enum class VodCardStyle {
    RECT,   // 矩形卡片（横版海报）
    OVAL,   // 圆形卡片（头像/圆形封面）
    LIST    // 列表卡片（横向列表项）
}

/**
 * 内容卡片组件
 * 对应现有 VodRectHolder / VodOvalHolder / VodListHolder
 * @param ratio 图片宽高比（对应 Style.getRatio()），默认 0.75f
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VodCard(
    vod: Vod,
    style: VodCardStyle = VodCardStyle.RECT,
    onClick: (Vod) -> Unit = {},
    onLongClick: (Vod) -> Unit = {},
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    ratio: Float = 0.75f
) {
    when (style) {
        VodCardStyle.RECT -> VodRectCard(vod, onClick, onLongClick, modifier, width, height, ratio)
        VodCardStyle.OVAL -> VodOvalCard(vod, onClick, onLongClick, modifier, width, height, ratio)
        VodCardStyle.LIST -> VodListCard(vod, onClick, onLongClick, modifier, width, height)
    }
}

/**
 * 矩形卡片（横版海报）
 * @param ratio 图片宽高比（对应 Style.getRatio()）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VodRectCard(
    vod: Vod,
    onClick: (Vod) -> Unit,
    onLongClick: (Vod) -> Unit,
    modifier: Modifier,
    width: Dp?,
    height: Dp?,
    ratio: Float = 0.75f
) {
    Card(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .combinedClickable(
                onClick = { onClick(vod) },
                onLongClick = { onLongClick(vod) }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (height != null) Modifier.height(height) else Modifier.aspectRatio(ratio))
            ) {
                ImageAsync(
                    url = vod.getPic(),
                    contentDescription = vod.getName(),
                    modifier = Modifier.fillMaxSize()
                )
                // 备注标签
                if (vod.getRemarks().isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = vod.getRemarks(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                // 名称
                Text(
                    text = vod.getName(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 站点名称（对应 Java 原版的 site 字段）
                if (vod.getSiteName().isNotEmpty() && vod.getSiteVisible() == 0) {
                    Text(
                        text = vod.getSiteName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 年份
                if (vod.getYear().isNotEmpty()) {
                    Text(
                        text = vod.getYear(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 圆形卡片（头像/圆形封面）
 * @param ratio 图片宽高比（对应 Style.getRatio()）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VodOvalCard(
    vod: Vod,
    onClick: (Vod) -> Unit,
    onLongClick: (Vod) -> Unit,
    modifier: Modifier,
    width: Dp?,
    height: Dp?,
    ratio: Float = 1.0f
) {
    Column(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.width(100.dp))
            .combinedClickable(
                onClick = { onClick(vod) },
                onLongClick = { onLongClick(vod) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(height ?: 80.dp)
                .clip(CircleShape)
        ) {
            ImageAsync(
                url = vod.getPic(),
                contentDescription = vod.getName(),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // 名称（对应 Java 原版的 name 字段）
        Text(
            text = vod.getName(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 列表卡片（横向列表项）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VodListCard(
    vod: Vod,
    onClick: (Vod) -> Unit,
    onLongClick: (Vod) -> Unit,
    modifier: Modifier,
    width: Dp?,
    height: Dp?
) {
    Card(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height ?: 100.dp)
            .combinedClickable(
                onClick = { onClick(vod) },
                onLongClick = { onLongClick(vod) }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
            ) {
                ImageAsync(
                    url = vod.getPic(),
                    contentDescription = vod.getName(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // 名称（对应 Java 原版的 name 字段）
                Text(
                    text = vod.getName(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 备注（对应 Java 原版的 remark 字段）
                if (vod.getRemarks().isNotEmpty()) {
                    Text(
                        text = vod.getRemarks(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
