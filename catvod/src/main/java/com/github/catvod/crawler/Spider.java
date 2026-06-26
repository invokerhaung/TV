package com.github.catvod.crawler;

import android.content.Context;

import com.github.catvod.net.OkHttp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

/**
 * Spider 抽象基类 - 所有爬虫的基础接口
 *
 * 定义了爬虫的完整生命周期方法，所有方法都有默认空实现，子类按需覆写。
 * 支持三种语言实现：Java（jar）、JavaScript（js）、Python（py）
 *
 * 生命周期：init → homeContent/homeVideoContent/categoryContent/detailContent/searchContent/playerContent → destroy
 */
public abstract class Spider {

    /** 站点标识，由 BaseLoader 在加载时自动设置 */
    public String siteKey;

    public static Dns safeDns() {
        return OkHttp.dns();
    }

    public static OkHttpClient client() {
        return OkHttp.client();
    }

    /**
     * 初始化爬虫（不带扩展配置）
     * @param context Android 上下文
     */
    public void init(Context context) throws Exception {
    }

    /**
     * 初始化爬虫（带扩展配置）- 由 BaseLoader 在加载 Spider 时调用
     * @param context Android 上下文
     * @param extend 扩展配置 JSON 字符串，来自 config.json 中 site 的 ext 字段
     */
    public void init(Context context, String extend) throws Exception {
        init(context);
    }

    /**
     * 获取首页内容 - 包含分类列表和筛选条件
     * @param filter 是否需要筛选条件
     * @return JSON 字符串，格式：{"class":[{"type_id":"1","type_name":"电影"}], "filters":{...}}
     */
    public String homeContent(boolean filter) throws Exception {
        return "";
    }

    /**
     * 获取首页推荐视频 - 用于首页展示推荐内容
     * @return JSON 字符串，格式：{"list":[{"vod_id":"xxx","vod_name":"xxx",...}]}
     */
    public String homeVideoContent() throws Exception {
        return "";
    }

    /**
     * 获取分类下的视频列表 - 支持翻页和筛选
     * @param tid 分类 ID
     * @param pg 页码
     * @param filter 是否使用筛选
     * @param extend 筛选条件扩展参数
     * @return JSON 字符串，格式：{"list":[...], "page":1, "pagecount":10, "limit":20, "total":200}
     */
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        return "";
    }

    /**
     * 获取视频详情 - 包含播放源、剧集列表等
     * @param ids 视频 ID 列表，通常只有一个元素
     * @return JSON 字符串，格式：{"list":[{"vod_id":"xxx","vod_play_from":"source1$$source2","vod_play_url":"ep1$url1$$ep2$url2"}]}
     */
    public String detailContent(List<String> ids) throws Exception {
        return "";
    }

    /**
     * 搜索视频（第一页）
     * @param key 搜索关键词
     * @param quick 是否快速搜索
     * @return JSON 字符串，格式同 categoryContent
     */
    public String searchContent(String key, boolean quick) throws Exception {
        return "";
    }

    /**
     * 搜索视频（带分页）
     * @param key 搜索关键词
     * @param quick 是否快速搜索
     * @param pg 页码
     * @return JSON 字符串，格式同 categoryContent
     */
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return "";
    }

    /**
     * 获取播放地址 - 根据播放源和视频ID获取实际播放URL
     * @param flag 播放源名称（如"量子m3u8"）
     * @param id 视频/剧集 ID
     * @param vipFlags VIP 标识列表
     * @return JSON 字符串，格式：{"url":"https://...", "parse":0, "header":{...}}
     */
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return "";
    }

    /**
     * 获取直播源内容
     * @param url 直播源地址
     * @return 直播源内容（M3U/TXT 格式）
     */
    public String liveContent(String url) throws Exception {
        return "";
    }

    /**
     * 是否使用自定义视频检测（嗅探）
     * @return true 表示使用自定义检测，false 表示使用默认检测
     */
    public boolean manualVideoCheck() throws Exception {
        return false;
    }

    /**
     * 判断 URL 是否为视频格式
     * @param url 待检测的 URL
     * @return true 表示是视频格式
     */
    public boolean isVideoFormat(String url) throws Exception {
        return false;
    }

    /**
     * 本地代理处理 - 用于处理需要代理的请求
     * @param params 代理参数
     * @return Object[] 结果数组：[contentType, statusCode, inputStream, headers]
     */
    public Object[] proxy(Map<String, String> params) throws Exception {
        return null;
    }

    /**
     * 执行自定义动作
     * @param action 动作标识
     * @return 动作执行结果
     */
    public String action(String action) throws Exception {
        return null;
    }

    /**
     * 销毁爬虫 - 释放资源，由 BaseLoader.clear() 调用
     */
    public void destroy() {
    }
}
