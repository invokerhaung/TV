package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R

/**
 * 图标按钮组件
 * 用于 TopAppBar 中的图标按钮
 * 原名 ImageButton，重命名为 IconBarButton 以避免与 Compose 原生组件混淆
 */
@Composable
fun IconBarButton(
    resId: Int,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

/**
 * 顶部应用栏组件
 * 对应现有 MaterialToolbar
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconBarButton(
                resId = R.drawable.ic_control_back,
                onClick = onBackClick,
                contentDescription = "返回"
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        actions()
    }
}
