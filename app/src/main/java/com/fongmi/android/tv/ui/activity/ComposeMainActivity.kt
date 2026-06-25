package com.fongmi.android.tv.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fongmi.android.tv.ui.compose.screen.AppScreen

/**
 * Compose 主 Activity
 * 作为应用入口，处理所有 intent-filter
 */
class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            AppScreen()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                // 处理分享的文本
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrEmpty()) {
                    // TODO: 导航到视频播放页面
                }
            }
            Intent.ACTION_VIEW -> {
                // 处理 deep link
                val uri = intent.data
                if (uri != null) {
                    handleDeepLink(uri)
                }
            }
            Intent.ACTION_SEARCH -> {
                // 处理搜索
                val query = intent.getStringExtra(SearchManager.QUERY)
                if (!query.isNullOrEmpty()) {
                    // TODO: 导航到搜索页面并执行搜索
                }
            }
        }
    }

    private fun handleDeepLink(uri: Uri) {
        when (uri.scheme) {
            "http", "https" -> {
                // 处理 HTTP/HTTPS 链接
                // TODO: 导航到视频播放页面
            }
            "smb", "rtmp", "rtsp" -> {
                // 处理流媒体链接
                // TODO: 导航到视频播放页面
            }
            "ed2k", "magnet", "thunder", "jianpian" -> {
                // 处理下载链接
                // TODO: 导航到视频播放页面
            }
            "content", "file" -> {
                // 处理本地文件
                // TODO: 导航到视频播放页面
            }
        }
    }
}
