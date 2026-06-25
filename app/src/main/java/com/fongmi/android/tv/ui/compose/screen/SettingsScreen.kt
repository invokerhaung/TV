package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.R
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.config.WallConfig
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.db.AppDatabase
import com.fongmi.android.tv.impl.Callback
import com.fongmi.android.tv.setting.Setting
import com.fongmi.android.tv.ui.compose.component.IconBarButton
import com.fongmi.android.tv.ui.compose.dialog.ConfigDialog
import com.fongmi.android.tv.ui.compose.dialog.HistoryDialog
import com.fongmi.android.tv.ui.compose.dialog.LiveDialog
import com.fongmi.android.tv.ui.compose.dialog.RestoreDialog
import com.fongmi.android.tv.ui.compose.dialog.SiteDialog
import com.fongmi.android.tv.ui.compose.dialog.ThemeDialog
import com.fongmi.android.tv.utils.Notify

/**
 * 设置屏幕
 * 对应 SettingFragment
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onPlayerSettings: () -> Unit = {},
    onDanmakuSettings: () -> Unit = {},
    onAbout: () -> Unit = {}
) {
    // 对话框状态
    var showVodConfigDialog by remember { mutableStateOf(false) }
    var showLiveConfigDialog by remember { mutableStateOf(false) }
    var showWallConfigDialog by remember { mutableStateOf(false) }
    var showSiteDialog by remember { mutableStateOf(false) }
    var showLiveDialog by remember { mutableStateOf(false) }
    var showVodHistoryDialog by remember { mutableStateOf(false) }
    var showLiveHistoryDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    // 开关状态
    var isIncognito by remember { mutableStateOf(Setting.isIncognito()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBarButton(
                resId = R.drawable.ic_control_back,
                onClick = onBackClick,
                contentDescription = "返回",
                tint = Color.White
            )
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 配置管理
            SettingsSection(title = "配置管理") {
                SettingsItem(
                    title = "视频配置",
                    subtitle = VodConfig.get().getConfig().getUrl().ifEmpty { "未设置" },
                    onClick = { showVodConfigDialog = true }
                )
                SettingsItem(
                    title = "直播配置",
                    subtitle = LiveConfig.get().getConfig().getUrl().ifEmpty { "未设置" },
                    onClick = { showLiveConfigDialog = true }
                )
                SettingsItem(
                    title = "壁纸配置",
                    subtitle = WallConfig.get().getConfig().getUrl().ifEmpty { "未设置" },
                    onClick = { showWallConfigDialog = true }
                )
            }

            // 站点选择
            SettingsSection(title = "站点选择") {
                SettingsItem(
                    title = "视频站点",
                    subtitle = VodConfig.get().getHome().getName().ifEmpty { "默认" },
                    onClick = { showSiteDialog = true }
                )
                SettingsItem(
                    title = "直播源",
                    subtitle = LiveConfig.get().getHome().getName().ifEmpty { "默认" },
                    onClick = { showLiveDialog = true }
                )
            }

            // 历史记录
            SettingsSection(title = "历史记录") {
                SettingsItem(
                    title = "视频历史",
                    subtitle = "查看视频配置历史",
                    onClick = { showVodHistoryDialog = true }
                )
                SettingsItem(
                    title = "直播历史",
                    subtitle = "查看直播配置历史",
                    onClick = { showLiveHistoryDialog = true }
                )
            }

            // 播放设置
            SettingsSection(title = "播放设置") {
                SettingsItem(
                    title = "播放器设置",
                    subtitle = "解码方式、缓冲策略等",
                    onClick = onPlayerSettings
                )
                SettingsItem(
                    title = "弹幕设置",
                    subtitle = "透明度、字号、速度等",
                    onClick = onDanmakuSettings
                )
            }

            // 其他设置
            SettingsSection(title = "其他") {
                // 无痕模式
                SettingsSwitchItem(
                    title = "无痕模式",
                    subtitle = "不记录播放历史",
                    checked = isIncognito,
                    onCheckedChange = {
                        isIncognito = it
                        Setting.putIncognito(it)
                    }
                )

                // 主题颜色
                SettingsItem(
                    title = "主题颜色",
                    subtitle = "自定义主题颜色",
                    onClick = { showThemeDialog = true }
                )

                // 数据管理
                SettingsItem(
                    title = "数据备份",
                    subtitle = "备份数据到本地",
                    onClick = { AppDatabase.backup() }
                )

                // 数据恢复
                SettingsItem(
                    title = "数据恢复",
                    subtitle = "从备份文件恢复数据",
                    onClick = { showRestoreDialog = true }
                )

                // 关于
                SettingsItem(
                    title = "关于",
                    subtitle = "版本信息、开源协议",
                    onClick = onAbout
                )
            }
        }
    }

    // 对话框
    if (showVodConfigDialog) {
        ConfigDialog(
            config = VodConfig.get().getConfig(),
            type = 0,
            onConfirm = { name, url ->
                showVodConfigDialog = false
                // 保存配置（对应 Java 原版 ConfigListener.setConfig）
                if (url.isNotEmpty()) {
                    val config = Config.find(url, 0)
                    if (name.isNotEmpty()) config.name(name)
                    config.update()
                    VodConfig.load(config, object : Callback() {
                        override fun success() {
                            // 配置加载成功
                        }
                    })
                }
            },
            onDismiss = { showVodConfigDialog = false }
        )
    }

    if (showLiveConfigDialog) {
        ConfigDialog(
            config = LiveConfig.get().getConfig(),
            type = 1,
            onConfirm = { name, url ->
                showLiveConfigDialog = false
                // 保存配置
                if (url.isNotEmpty()) {
                    val config = Config.find(url, 1)
                    if (name.isNotEmpty()) config.name(name)
                    config.update()
                    LiveConfig.load(config, object : Callback() {
                        override fun success() {
                            // 配置加载成功
                        }
                    })
                }
            },
            onDismiss = { showLiveConfigDialog = false }
        )
    }

    if (showWallConfigDialog) {
        ConfigDialog(
            config = WallConfig.get().getConfig(),
            type = 2,
            onConfirm = { name, url ->
                showWallConfigDialog = false
                // 保存配置
                if (url.isNotEmpty()) {
                    val config = Config.find(url, 2)
                    if (name.isNotEmpty()) config.name(name)
                    config.update()
                    WallConfig.load(config, object : Callback() {
                        override fun success() {
                            // 配置加载成功
                        }
                    })
                }
            },
            onDismiss = { showWallConfigDialog = false }
        )
    }

    if (showSiteDialog) {
        SiteDialog(
            sites = VodConfig.get().getSites(),
            change = true,
            onSiteClick = { site ->
                VodConfig.get().setHome(site)
                showSiteDialog = false
            },
            onSearchToggle = { _, _ -> },
            onChangeToggle = { _, _ -> },
            onDismiss = { showSiteDialog = false }
        )
    }

    if (showLiveDialog) {
        LiveDialog(
            lives = LiveConfig.get().getLives(),
            onLiveClick = { live ->
                LiveConfig.get().setHome(live)
                showLiveDialog = false
            },
            onBootToggle = { _, _ -> },
            onPassToggle = { _, _ -> },
            onDismiss = { showLiveDialog = false }
        )
    }

    if (showVodHistoryDialog) {
        HistoryDialog(
            configs = Config.getAll(0),
            onConfigClick = { config ->
                showVodHistoryDialog = false
                // 加载配置（对应 Java 原版 ConfigListener.setConfig）
                VodConfig.load(config, object : Callback() {
                    override fun success() {
                        // 配置加载成功
                    }
                })
            },
            onDeleteClick = { },
            onDismiss = { showVodHistoryDialog = false }
        )
    }

    if (showLiveHistoryDialog) {
        HistoryDialog(
            configs = Config.getAll(1),
            onConfigClick = { config ->
                showLiveHistoryDialog = false
                // 加载配置
                LiveConfig.load(config, object : Callback() {
                    override fun success() {
                        // 配置加载成功
                    }
                })
            },
            onDeleteClick = { },
            onDismiss = { showLiveHistoryDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentColor = Setting.getThemeColor(),
            onColorSelected = { color ->
                Setting.putThemeColor(color)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showRestoreDialog) {
        RestoreDialog(
            files = AppDatabase.getRestoreFiles(),
            onFileClick = { file ->
                AppDatabase.restore(file, object : Callback() {
                    override fun success() {
                        Notify.show("恢复成功")
                        showRestoreDialog = false
                    }

                    override fun error(msg: String) {
                        Notify.show(msg)
                    }
                })
            },
            onDeleteClick = { file ->
                file.delete()
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

/**
 * 设置分组
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_widget_forward),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.5f))
        )
    }
}

/**
 * 设置开关项
 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
