package com.fongmi.android.tv.ui.compose.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 形状定义
 * 从 styles.xml 迁移的圆角样式
 */
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

/**
 * Vod 卡片形状
 * 对应 styles.xml 中的 Vod 样式
 */
object VodShape {
    /**
     * 网格卡片形状（顶部圆角）
     * 对应 Vod.Grid 样式
     */
    val Grid = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    /**
     * 网格卡片形状（大圆角）
     * 对应 Vod.Grid.Large 样式
     */
    val GridLarge = RoundedCornerShape(8.dp)

    /**
     * 圆形形状
     * 对应 Vod.Circle 样式
     */
    val Circle = CircleShape

    /**
     * 列表卡片形状（左侧圆角）
     * 对应 Vod.List 样式
     */
    val List = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 0.dp,
        bottomStart = 8.dp,
        bottomEnd = 0.dp
    )
}

/**
 * 播放器配置
 * 对应 styles.xml 中的 Player 样式
 */
object PlayerConfig {
    /**
     * 默认播放器配置
     * 对应 Player 样式
     */
    const val RESIZE_MODE = "fit"
    const val USE_ARTWORK = true
    const val USE_CONTROLLER = false

    /**
     * 视频播放器配置
     * 对应 Player.Vod 样式
     */
    const val KEEP_CONTENT_ON_PLAYER_RESET_VOD = false

    /**
     * 直播播放器配置
     * 对应 Player.Live 样式
     */
    const val KEEP_CONTENT_ON_PLAYER_RESET_LIVE = true
}
