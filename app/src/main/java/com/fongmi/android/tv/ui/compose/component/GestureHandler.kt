package com.fongmi.android.tv.ui.compose.component

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.fongmi.android.tv.utils.ResUtil

/**
 * 手势控制回调接口
 * 对应 CustomKeyDown.Listener
 */
interface GestureListener {
    fun onSeeking(time: Long) {}
    fun onSeekEnd(time: Long) {}
    fun onSpeedUp() {}
    fun onSpeedEnd() {}
    fun onBright(progress: Int) {}
    fun onVolume(progress: Int) {}
    fun onFlingUp() {}
    fun onFlingDown() {}
    fun onSingleTap() {}
    fun onDoubleTap() {}
    fun onTouchEnd() {}
}

/**
 * 手势控制修饰符
 * 对应 CustomKeyDown
 */
@Composable
fun Modifier.gestureControl(
    activity: Activity,
    listener: GestureListener,
    isLocked: Boolean = false
): Modifier {
    val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    var isChangingBright by remember { mutableStateOf(false) }
    var isChangingVolume by remember { mutableStateOf(false) }
    var isChangingTime by remember { mutableStateOf(false) }
    var isChangingSpeed by remember { mutableStateOf(false) }

    var startBright by remember { mutableFloatStateOf(0f) }
    var startVolume by remember { mutableFloatStateOf(0f) }
    var startTime by remember { mutableFloatStateOf(0f) }

    // 双指缩放
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        if (!isLocked) {
            // 缩放处理
        }
    }

    return this
        .transformable(state = transformableState)
        .pointerInput(isLocked) {
            if (isLocked) return@pointerInput

            detectTapGestures(
                onTap = {
                    listener.onSingleTap()
                },
                onDoubleTap = {
                    listener.onDoubleTap()
                },
                onLongPress = {
                    listener.onSpeedUp()
                    isChangingSpeed = true
                }
            )
        }
        .pointerInput(isLocked) {
            if (isLocked) return@pointerInput

            detectDragGestures(
                onDragStart = { offset ->
                    val screenWidth = size.width.toFloat()
                    val screenHeight = size.height.toFloat()

                    isChangingBright = false
                    isChangingVolume = false
                    isChangingTime = false

                    // 判断手势类型
                    if (offset.x < screenWidth / 4) {
                        // 左侧 - 亮度
                        isChangingBright = true
                        startBright = activity.window.attributes.screenBrightness
                        if (startBright < 0) startBright = 0.5f
                    } else if (offset.x > screenWidth * 3 / 4) {
                        // 右侧 - 音量
                        isChangingVolume = true
                        startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                    } else {
                        // 中间 - 进度
                        isChangingTime = true
                        startTime = 0f
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()

                    val deltaY = -dragAmount.y
                    val deltaX = dragAmount.x
                    val screenHeight = size.height.toFloat()

                    when {
                        isChangingBright -> {
                            val brightness = (deltaY * 2.0f / screenHeight + startBright).coerceIn(0f, 1f)
                            val layoutParams = activity.window.attributes
                            layoutParams.screenBrightness = brightness
                            activity.window.attributes = layoutParams
                            listener.onBright((brightness * 100).toInt())
                        }
                        isChangingVolume -> {
                            val deltaV = deltaY * 2.0f / screenHeight * maxVolume
                            val index = (startVolume + deltaV).coerceIn(0f, maxVolume.toFloat())
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index.toInt(), 0)
                            listener.onVolume((index / maxVolume * 100).toInt())
                        }
                        isChangingTime -> {
                            val time = (deltaX * 50).toLong()
                            listener.onSeeking(time)
                        }
                    }
                },
                onDragEnd = {
                    when {
                        isChangingTime -> {
                            listener.onSeekEnd(0L)
                        }
                        isChangingSpeed -> {
                            listener.onSpeedEnd()
                        }
                    }
                    isChangingBright = false
                    isChangingVolume = false
                    isChangingTime = false
                    isChangingSpeed = false
                    listener.onTouchEnd()
                },
                onDragCancel = {
                    isChangingBright = false
                    isChangingVolume = false
                    isChangingTime = false
                    isChangingSpeed = false
                    listener.onTouchEnd()
                }
            )
        }
}
