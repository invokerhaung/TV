package com.fongmi.android.tv.ui.compose.util

import android.graphics.Color
import android.text.TextUtils
import com.fongmi.android.tv.utils.UrlUtil
import com.google.common.net.HttpHeaders
import okhttp3.Headers

/**
 * 图片加载工具类
 * 复用 Java 原版 ImgUtil 的逻辑
 */
object ImgUtil {

    /**
     * 解析 URL 中的自定义 Headers
     * 支持格式：
     * - @Headers={"key":"value"}
     * - @Cookie=value
     * - @Referer=value
     * - @User-Agent=value
     */
    fun parseUrl(url: String): Pair<String, Headers?> {
        if (TextUtils.isEmpty(url)) return Pair("", null)

        var parsedUrl = UrlUtil.convert(url)
        val builder = Headers.Builder()
        var hasHeaders = false

        // 解析 @Headers=
        if (parsedUrl.contains("@Headers=")) {
            try {
                val headerJson = parsedUrl.split("@Headers=")[1].split("@")[0]
                val headerMap = com.github.catvod.utils.Json.toMap(com.github.catvod.utils.Json.parse(headerJson))
                for ((key, value) in headerMap) {
                    builder.add(UrlUtil.fixHeader(key), value)
                    hasHeaders = true
                }
            } catch (_: Exception) {}
        }

        // 解析 @Cookie=
        if (parsedUrl.contains("@Cookie=")) {
            try {
                val cookie = parsedUrl.split("@Cookie=")[1].split("@")[0]
                builder.add(HttpHeaders.COOKIE, cookie)
                hasHeaders = true
            } catch (_: Exception) {}
        }

        // 解析 @Referer=
        if (parsedUrl.contains("@Referer=")) {
            try {
                val referer = parsedUrl.split("@Referer=")[1].split("@")[0]
                builder.add(HttpHeaders.REFERER, referer)
                hasHeaders = true
            } catch (_: Exception) {}
        }

        // 解析 @User-Agent=
        if (parsedUrl.contains("@User-Agent=")) {
            try {
                val userAgent = parsedUrl.split("@User-Agent=")[1].split("@")[0]
                builder.add(HttpHeaders.USER_AGENT, userAgent)
                hasHeaders = true
            } catch (_: Exception) {}
        }

        // 移除 URL 中的 Headers 标记
        if (hasHeaders) {
            parsedUrl = parsedUrl.split("@")[0]
        }

        return Pair(parsedUrl, if (hasHeaders) builder.build() else null)
    }

    /**
     * 根据文字生成颜色
     * 复用 Java 原版 ColorGenerator 的逻辑
     */
    fun getColorForText(text: String): Int {
        val colors = intArrayOf(
            Color.parseColor("#EF5350"), // RED_400
            Color.parseColor("#EC407A"), // PINK_400
            Color.parseColor("#AB47BC"), // PURPLE_400
            Color.parseColor("#7E57C2"), // DEEP_PURPLE_400
            Color.parseColor("#5C6BC0"), // INDIGO_400
            Color.parseColor("#42A5F5"), // BLUE_400
            Color.parseColor("#29B6F6"), // LIGHT_BLUE_400
            Color.parseColor("#26C6DA"), // CYAN_400
            Color.parseColor("#26A69A"), // TEAL_400
            Color.parseColor("#66BB6A"), // GREEN_400
            Color.parseColor("#9CCC65"), // LIGHT_GREEN_400
            Color.parseColor("#D4E157"), // LIME_400
            Color.parseColor("#FFEE58"), // YELLOW_400
            Color.parseColor("#FFCA28"), // AMBER_400
            Color.parseColor("#FFA726"), // ORANGE_400
            Color.parseColor("#FF7043"), // DEEP_ORANGE_400
            Color.parseColor("#8D6E63"), // BROWN_400
            Color.parseColor("#BDBDBD"), // GREY_400
            Color.parseColor("#78909C"), // BLUE_GREY_400
        )
        val index = (text.hashCode() and Int.MAX_VALUE) % colors.size
        return colors[index]
    }

    /**
     * 获取文字占位符的首字
     */
    fun getPlaceholderText(text: String?): String {
        if (TextUtils.isEmpty(text)) return "！"
        return text!!.substring(0, 1)
    }
}
