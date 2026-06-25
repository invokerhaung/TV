package com.fongmi.android.tv.ui.compose.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 加载状态枚举
 * 对应 Java 原版 ProgressLayout.State
 */
enum class LoadingStateType {
    CONTENT,  // 显示内容
    LOADING,  // 加载中
    EMPTY     // 空状态
}

/**
 * 加载状态组件
 * 对应现有 ProgressLayout，支持加载中/空状态/内容状态
 */
@Composable
fun LoadingState(
    state: LoadingStateType,
    modifier: Modifier = Modifier,
    emptyMessage: String = "暂无数据",
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 内容
        AnimatedVisibility(
            visible = state == LoadingStateType.CONTENT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            content()
        }

        // 加载中
        AnimatedVisibility(
            visible = state == LoadingStateType.LOADING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 空状态
        AnimatedVisibility(
            visible = state == LoadingStateType.EMPTY,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 根据数据状态自动判断加载状态
 * 对应 Java 原版 ProgressLayout.showContent(boolean flag, int size)
 *
 * @param isLoading 是否正在加载
 * @param data 数据列表
 * @param emptyMessage 空状态提示信息
 * @param content 内容组件
 */
@Composable
fun <T> LoadingStateAuto(
    isLoading: Boolean,
    data: List<T>?,
    modifier: Modifier = Modifier,
    emptyMessage: String = "暂无数据",
    content: @Composable () -> Unit
) {
    val state = when {
        isLoading -> LoadingStateType.LOADING
        data == null -> LoadingStateType.LOADING
        data.isEmpty() -> LoadingStateType.EMPTY
        else -> LoadingStateType.CONTENT
    }

    LoadingState(
        state = state,
        modifier = modifier,
        emptyMessage = emptyMessage,
        content = content
    )
}

/**
 * 根据数据状态自动判断加载状态（简化版）
 * 对应 Java 原版 ProgressLayout.showContent(boolean flag, int size)
 *
 * @param hasData 是否有数据
 * @param isLoading 是否正在加载
 * @param emptyMessage 空状态提示信息
 * @param content 内容组件
 */
@Composable
fun LoadingStateSimple(
    hasData: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    emptyMessage: String = "暂无数据",
    content: @Composable () -> Unit
) {
    val state = when {
        isLoading -> LoadingStateType.LOADING
        !hasData -> LoadingStateType.EMPTY
        else -> LoadingStateType.CONTENT
    }

    LoadingState(
        state = state,
        modifier = modifier,
        emptyMessage = emptyMessage,
        content = content
    )
}
