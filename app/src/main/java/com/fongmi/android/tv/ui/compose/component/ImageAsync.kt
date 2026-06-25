package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fongmi.android.tv.ui.compose.util.ImgUtil

/**
 * 异步图片加载组件
 * 替换 Glide，使用 Coil 实现
 * 支持失败文字占位符
 */
@Composable
fun ImageAsync(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    showTextPlaceholder: Boolean = true
) {
    val context = LocalContext.current

    // 生成文字占位符
    val placeholderText = ImgUtil.getPlaceholderText(contentDescription)
    val placeholderColor = Color(ImgUtil.getColorForText(placeholderText))
    val textPainter = ColorPainter(placeholderColor)

    // 构建 ImageRequest
    val requestBuilder = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = requestBuilder.build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            placeholder = placeholder ?: if (showTextPlaceholder) textPainter else null,
            error = error ?: if (showTextPlaceholder) textPainter else null,
            contentScale = contentScale
        )
    }
}

/**
 * 带文字占位符的异步图片组件
 * 失败时显示首字 + 颜色背景
 */
@Composable
fun ImageAsyncWithPlaceholder(
    url: String,
    text: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // 生成文字占位符
    val placeholderText = ImgUtil.getPlaceholderText(text)
    val placeholderColor = Color(ImgUtil.getColorForText(placeholderText))

    // 构建 ImageRequest
    val requestBuilder = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(placeholderColor),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = requestBuilder.build(),
            contentDescription = text,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )

        // 文字占位符（在图片加载成功后会被覆盖）
        Text(
            text = placeholderText,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
