package com.fongmi.android.tv.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.fongmi.android.tv.setting.Setting
import com.fongmi.android.tv.ui.compose.screen.AppScreen
import com.fongmi.android.tv.ui.custom.CustomWallView
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

/**
 * Compose 主 Activity
 * 作为应用入口，处理所有 intent-filter
 * 对应 Java 原版 BaseActivity 的功能
 *
 * 注意：EventBus 事件处理已移至各 Screen 的 DisposableEffect 中
 */
class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        enableDynamicColor()
        super.onCreate(savedInstanceState)

        // 设置返回键回调（对应 Java 原版 BaseActivity.setBackCallback）
        setBackCallback()

        handleIntent(intent)

        // 添加自定义壁纸（对应 Java 原版 BaseActivity.setContentView）
        // 注意：必须在 setContent 之前添加，这样壁纸才能在 Compose 内容下面显示
        if (customWall()) {
            addCustomWallView()
        }

        setContent {
            AppScreen()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * 启用边到边显示（对应 Java 原版 BaseActivity.enableEdgeToEdge）
     */
    private fun enableEdgeToEdge() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }

    /**
     * 启用动态颜色（对应 Java 原版 BaseActivity.enableDynamicColor）
     */
    private fun enableDynamicColor() {
        val color = Setting.getDynamicColor()
        if (color != 0) {
            DynamicColors.applyToActivityIfAvailable(
                this,
                DynamicColorsOptions.Builder().setContentBasedSource(color).build()
            )
        }
    }

    /**
     * 是否启用自定义壁纸（对应 Java 原版 BaseActivity.customWall）
     */
    private fun customWall(): Boolean {
        return true
    }

    /**
     * 添加自定义壁纸视图（对应 Java 原版 BaseActivity.setContentView）
     */
    private fun addCustomWallView() {
        val contentView = findViewById<ViewGroup>(android.R.id.content)
        val wallView = CustomWallView(this, null)
        contentView.addView(wallView, 0, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }

    /**
     * 设置返回键回调（对应 Java 原版 BaseActivity.setBackCallback）
     */
    private fun setBackCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackInvoked()
            }
        })
    }

    /**
     * 返回键处理（对应 Java 原版 BaseActivity.onBackInvoked）
     */
    protected fun onBackInvoked() {
        finish()
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

    override fun onDestroy() {
        super.onDestroy()
    }
}
