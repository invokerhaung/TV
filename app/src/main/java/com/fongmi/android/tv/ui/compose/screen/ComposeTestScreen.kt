package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fongmi.android.tv.model.SiteViewModel

/**
 * 验证用 Compose Screen
 * 测试 observeAsState() 能否正常读取 ViewModel 的 LiveData
 */
@Composable
fun ComposeTestScreen(
    modifier: Modifier = Modifier,
    siteViewModel: SiteViewModel = viewModel()
) {
    val result by siteViewModel.result.observeAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (result != null) {
                    "LiveData 已加载: ${result?.getList()?.size ?: 0} 条数据"
                } else {
                    "等待加载数据..."
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
