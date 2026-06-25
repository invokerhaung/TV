package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.ui.compose.theme.TVTheme

/**
 * 组件预览文件
 * 用于在 Android Studio 中预览各个组件
 */

// ==================== LoadingState 预览 ====================

@Preview(showBackground = true)
@Composable
fun LoadingStatePreview() {
    TVTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "加载状态预览", style = MaterialTheme.typography.headlineMedium)

                LoadingState(
                    state = LoadingStateType.LOADING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {}

                LoadingState(
                    state = LoadingStateType.EMPTY,
                    emptyMessage = "暂无内容",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {}

                LoadingState(
                    state = LoadingStateType.CONTENT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(text = "这是内容区域")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingStateSimplePreview() {
    TVTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "简化版加载状态预览", style = MaterialTheme.typography.headlineMedium)

                LoadingStateSimple(
                    hasData = false,
                    isLoading = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {}

                LoadingStateSimple(
                    hasData = false,
                    isLoading = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {}

                LoadingStateSimple(
                    hasData = true,
                    isLoading = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(text = "这是内容区域")
                }
            }
        }
    }
}

// ==================== FilterBar 预览 ====================

@Preview(showBackground = true)
@Composable
fun FilterBarPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            val filters = listOf(
                FilterItem("1", "全部", true),
                FilterItem("2", "电影"),
                FilterItem("3", "电视剧"),
                FilterItem("4", "综艺"),
                FilterItem("5", "动漫")
            )

            FilterBar(
                filters = filters,
                onFilterSelected = {},
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "标签示例", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(text = "默认标签")
                    Tag(text = "选中标签", isSelected = true)
                }
            }
        }
    }
}

// ==================== TopBar 预览 ====================

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TVTheme {
        TopBar(
            title = "示例标题",
            onBackClick = {}
        )
    }
}

// ==================== NavBar 预览 ====================

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    TVTheme {
        NavBar(
            items = listOf(
                NavItem("home", "首页", com.fongmi.android.tv.R.drawable.ic_nav_vod),
                NavItem("live", "直播", com.fongmi.android.tv.R.drawable.ic_nav_live),
                NavItem("search", "搜索", com.fongmi.android.tv.R.drawable.ic_action_search),
                NavItem("settings", "设置", com.fongmi.android.tv.R.drawable.ic_nav_setting)
            ),
            currentRoute = "home",
            onNavigate = {}
        )
    }
}

// ==================== Dialog 预览 ====================

@Preview(showBackground = true)
@Composable
fun AlertDialogPreview() {
    TVTheme {
        AppAlertDialog(
            title = "提示",
            message = "这是一个示例对话框",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

// ==================== VodCard 预览 ====================

@Preview(showBackground = true)
@Composable
fun VodRectCardPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.width(150.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Text(text = "矩形卡片", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // 注意：VodCard 需要 Vod 对象，这里仅预览布局结构
                // 实际使用时需要传入真实的 Vod 对象
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VodOvalCardPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Text(text = "圆形卡片", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // 注意：VodCard 需要 Vod 对象，这里仅预览布局结构
            }
        }
    }
}

// ==================== EpisodeItem 预览 ====================

@Preview(showBackground = true)
@Composable
fun EpisodeItemPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Text(text = "剧集项", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // 注意：EpisodeItem 需要 Episode 对象，这里仅预览布局结构
            }
        }
    }
}

// ==================== OverlayIndicator 预览 ====================

@Preview(showBackground = true)
@Composable
fun OverlayIndicatorPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.size(150.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            OverlayIndicator(
                icon = com.fongmi.android.tv.R.drawable.ic_widget_bright_high,
                progress = 75
            )
        }
    }
}

// ==================== ImageAsync 预览 ====================

@Preview(showBackground = true)
@Composable
fun ImageAsyncPreview() {
    TVTheme {
        Surface(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Text(text = "图片组件", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // 注意：ImageAsync 需要 URL，这里仅预览布局结构
            }
        }
    }
}
