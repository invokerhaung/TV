package com.fongmi.android.tv.ui.compose.component

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.media3.ui.R as Media3R
import com.fongmi.android.tv.R
import com.fongmi.android.tv.setting.PlayerSetting

/**
 * PiP 画中画辅助类
 * 对应原版 PiP.java
 */
object PiPHelper {

    /**
     * 检查是否支持 PiP
     */
    fun isSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * 进入 PiP 模式
     */
    fun enter(activity: Activity, width: Int = 0, height: Int = 0, scale: Int = 0) {
        if (!isSupported()) return
        if (activity.isInPictureInPictureMode) return
        if (!PlayerSetting.isBackgroundPiP()) return

        try {
            val builder = PictureInPictureParams.Builder()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setSeamlessResizeEnabled(true)
            }

            // 设置宽高比
            val aspectRatio = when (scale) {
                1 -> Rational(16, 9)
                2 -> Rational(4, 3)
                else -> getRational(width, height)
            }
            builder.setAspectRatio(aspectRatio)

            activity.enterPictureInPictureMode(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 更新 PiP 参数
     */
    fun update(activity: Activity, view: View) {
        if (!isSupported()) return

        try {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val builder = PictureInPictureParams.Builder()
            builder.setSourceRectHint(rect)
            activity.setPictureInPictureParams(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 更新 PiP 参数（带 RemoteAction）
     * 对应 Java 原版 PiP.update(Activity, boolean)
     */
    fun update(activity: Activity, play: Boolean) {
        if (!isSupported()) return

        try {
            val builder = PictureInPictureParams.Builder()
            val actions = mutableListOf<RemoteAction>()

            // 添加音频切换按钮
            actions.add(buildRemoteAction(
                activity,
                R.drawable.ic_action_audio,
                Media3R.string.exo_controls_hide,
                "AUDIO"
            ))

            // 添加播放/暂停按钮
            actions.add(getPlayPauseAction(activity, play))

            // 添加下一集按钮
            actions.add(buildRemoteAction(
                activity,
                Media3R.drawable.exo_icon_next,
                Media3R.string.exo_controls_next_description,
                "NEXT"
            ))

            builder.setActions(actions)
            activity.setPictureInPictureParams(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 构建 RemoteAction
     */
    private fun buildRemoteAction(
        activity: Activity,
        @DrawableRes icon: Int,
        @StringRes title: Int,
        action: String
    ): RemoteAction {
        val intent = Intent(action).setPackage(activity.packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return RemoteAction(
            Icon.createWithResource(activity, icon),
            activity.getString(title),
            "",
            pendingIntent
        )
    }

    /**
     * 获取播放/暂停 RemoteAction
     */
    private fun getPlayPauseAction(activity: Activity, play: Boolean): RemoteAction {
        return if (play) {
            buildRemoteAction(
                activity,
                Media3R.drawable.exo_icon_pause,
                Media3R.string.exo_controls_pause_description,
                "PAUSE"
            )
        } else {
            buildRemoteAction(
                activity,
                Media3R.drawable.exo_icon_play,
                Media3R.string.exo_controls_play_description,
                "PLAY"
            )
        }
    }

    /**
     * 获取宽高比
     */
    private fun getRational(width: Int, height: Int): Rational {
        if (width <= 0 || height <= 0) return Rational(16, 9)
        val limitWide = Rational(239, 100)
        val limitTall = Rational(100, 239)
        val rational = Rational(width, height)
        if (rational.isInfinite) return Rational(16, 9)
        if (rational.toFloat() > limitWide.toFloat()) return limitWide
        if (rational.toFloat() < limitTall.toFloat()) return limitTall
        return rational
    }
}
