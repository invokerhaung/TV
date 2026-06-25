package com.fongmi.android.tv.ui.compose.component

import android.net.TrafficStats
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.App
import kotlinx.coroutines.delay
import java.text.DecimalFormat

/**
 * 流量监控指示器
 * 对应原版 Traffic.java
 */
@Composable
fun TrafficIndicator(
    modifier: Modifier = Modifier
) {
    var speedText by remember { mutableStateOf("") }
    var lastTotalRxBytes by remember { mutableLongStateOf(0L) }
    var lastTimeStamp by remember { mutableLongStateOf(0L) }
    val format = remember { DecimalFormat("#.0") }
    val uid = remember { App.get().applicationInfo.uid }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // 每秒更新一次
            val nowTimeStamp = System.currentTimeMillis()
            val nowTotalRxBytes = TrafficStats.getUidRxBytes(uid) / 1024

            if (lastTimeStamp > 0) {
                val speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp)
                speedText = if (speed < 1000) {
                    "${speed} KB/s"
                } else {
                    "${format.format(speed / 1024f)} MB/s"
                }
            }

            lastTimeStamp = nowTimeStamp
            lastTotalRxBytes = nowTotalRxBytes
        }
    }

    if (speedText.isNotEmpty()) {
        Text(
            text = speedText,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}
