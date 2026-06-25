package com.fongmi.android.tv.ui.compose.component

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.danmaku.DanmakuView

/**
 * 弹幕覆盖层组件
 * 使用 AndroidView 包裹现有弹幕 View
 */
@Composable
fun DanmakuOverlay(
    danmakuView: DanmakuView?,
    modifier: Modifier = Modifier
) {
    danmakuView?.let { view ->
        AndroidView(
            factory = { view },
            modifier = modifier.fillMaxSize()
        )
    }
}
