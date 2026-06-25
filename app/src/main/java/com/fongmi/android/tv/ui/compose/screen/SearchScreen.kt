package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fongmi.android.tv.R
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.bean.Word
import com.fongmi.android.tv.model.SiteViewModel
import com.fongmi.android.tv.setting.Setting
import com.fongmi.android.tv.ui.compose.component.IconBarButton
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType
import com.fongmi.android.tv.ui.compose.component.VodCard
import com.fongmi.android.tv.ui.compose.component.VodCardStyle
import com.fongmi.android.tv.ui.compose.dialog.SiteDialog
import com.fongmi.android.tv.utils.Util
import com.github.catvod.net.OkHttp
import com.google.common.net.HttpHeaders
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

/**
 * 搜索屏幕
 * 对应 SearchActivity + SearchFragment
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    initialKeyword: String = "",
    onBackClick: () -> Unit = {},
    onVodClick: (Vod) -> Unit = {},
    siteViewModel: SiteViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var keyword by remember { mutableStateOf(initialKeyword) }
    var showSiteDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<Site?>(null) }
    val searchResult by siteViewModel.search.observeAsState()

    // 搜索历史记录
    var searchHistory by remember { mutableStateOf(loadSearchHistory()) }

    // 热搜词和搜索建议
    var hotWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var suggestWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingWords by remember { mutableStateOf(false) }

    // 加载热搜词
    LaunchedEffect(Unit) {
        loadHotWords { words ->
            hotWords = words
        }
        // 自动弹出键盘（对应 Java 原版 Util.showKeyboard）
        if (initialKeyword.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 如果有初始关键词，自动执行搜索
    LaunchedEffect(initialKeyword) {
        if (initialKeyword.isNotEmpty()) {
            keyword = initialKeyword
            performSearch(siteViewModel, keyword, selectedSite)
        }
    }

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
            IconButton(onClick = onBackClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_back),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            OutlinedTextField(
                value = keyword,
                onValueChange = { newKeyword ->
                    keyword = newKeyword
                    // 获取搜索建议（对应 Java 原版 getSuggest）
                    if (newKeyword.isNotEmpty()) {
                        loadSuggestWords(newKeyword) { words ->
                            suggestWords = words
                        }
                    } else {
                        suggestWords = emptyList()
                    }
                },
                placeholder = { Text("搜索内容") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (keyword.isNotEmpty()) {
                            addSearchHistory(keyword)
                            searchHistory = loadSearchHistory()
                            performSearch(siteViewModel, keyword, selectedSite)
                            keyboardController?.hide()
                        }
                    }
                ),
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                keyword = ""
                                suggestWords = emptyList()
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_action_reset),
                                contentDescription = "清除",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }
                }
            )
            // 站点切换按钮
            IconButton(
                onClick = { showSiteDialog = true }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_site),
                    contentDescription = "站点",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        // 搜索历史和热搜词/搜索建议
        if (searchResult == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 搜索建议（输入时显示）
                if (suggestWords.isNotEmpty()) {
                    Text(
                        text = "搜索建议",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        suggestWords.forEach { word ->
                            Card(
                                onClick = {
                                    keyword = word
                                    addSearchHistory(word)
                                    searchHistory = loadSearchHistory()
                                    performSearch(siteViewModel, word, selectedSite)
                                    keyboardController?.hide()
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 搜索历史
                if (searchHistory.isNotEmpty() && suggestWords.isEmpty()) {
                    Text(
                        text = "搜索历史",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        searchHistory.take(10).forEach { record ->
                            Card(
                                onClick = {
                                    keyword = record
                                    performSearch(siteViewModel, keyword, selectedSite)
                                    keyboardController?.hide()
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = record,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 热搜词（对应 Java 原版 getHot）
                if (suggestWords.isEmpty()) {
                    Text(
                        text = "热搜词",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (hotWords.isEmpty()) {
                        Text(
                            text = "加载中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            hotWords.forEach { word ->
                                Card(
                                    onClick = {
                                        keyword = word
                                        addSearchHistory(word)
                                        searchHistory = loadSearchHistory()
                                        performSearch(siteViewModel, word, selectedSite)
                                        keyboardController?.hide()
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 搜索结果
            val state = when {
                searchResult?.getList()?.isEmpty() == true -> LoadingStateType.EMPTY
                else -> LoadingStateType.CONTENT
            }

            LoadingState(
                state = state,
                emptyMessage = "未找到相关内容",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                searchResult?.getList()?.let { vods ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(vods) { vod ->
                            VodCard(
                                vod = vod,
                                style = VodCardStyle.LIST,
                                onClick = { onVodClick(vod) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 站点切换对话框
    if (showSiteDialog) {
        SiteDialog(
            sites = com.fongmi.android.tv.api.config.VodConfig.get().getSites(),
            search = true,
            onSiteClick = { site ->
                selectedSite = site
                showSiteDialog = false
                if (keyword.isNotEmpty()) {
                    performSearch(siteViewModel, keyword, selectedSite)
                }
            },
            onSearchToggle = { _, _ -> },
            onChangeToggle = { _, _ -> },
            onDismiss = { showSiteDialog = false }
        )
    }
}

/**
 * 执行搜索
 */
private fun performSearch(
    siteViewModel: SiteViewModel,
    keyword: String,
    site: Site?
) {
    if (site != null) {
        siteViewModel.searchContent(site, keyword, false, "1")
    } else {
        // 搜索所有可搜索站点
        val sites = com.fongmi.android.tv.api.config.VodConfig.get().getSites()
        val searchableSites = sites.filter { it.isSearchable() }
        if (searchableSites.isNotEmpty()) {
            siteViewModel.searchContent(searchableSites.first(), keyword, false, "1")
        }
    }
}

/**
 * 加载搜索历史
 */
private fun loadSearchHistory(): List<String> {
    return com.fongmi.android.tv.setting.Setting.getKeyword().split(",").filter { it.isNotEmpty() }
}

/**
 * 添加搜索历史
 */
private fun addSearchHistory(keyword: String) {
    val history = loadSearchHistory().toMutableList()
    history.remove(keyword)
    history.add(0, keyword)
    com.fongmi.android.tv.setting.Setting.putKeyword(history.take(20).joinToString(","))
}

/**
 * 加载热搜词（对应 Java 原版 getHot）
 */
private fun loadHotWords(onResult: (List<String>) -> Unit) {
    // 先从缓存获取
    val cached = Setting.getHot()
    if (cached.isNotEmpty()) {
        try {
            val word = Word.objectFrom(cached)
            if (word != null && word.getData().isNotEmpty()) {
                onResult(word.getData().map { it.getTitle() })
            }
        } catch (_: Exception) {}
    }

    // 从网络获取
    val headers = mapOf(HttpHeaders.REFERER to "https://www.360kan.com/rank/general")
    OkHttp.newCall("https://api.web.360kan.com/v1/rank?cat=1", headers).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // 失败时使用缓存
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                val result = response.body?.string() ?: return
                if (result.isNotEmpty()) {
                    Setting.putHot(result)
                    val word = Word.objectFrom(result)
                    if (word != null && word.getData().isNotEmpty()) {
                        com.fongmi.android.tv.App.post {
                            onResult(word.getData().map { it.getTitle() })
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    })
}

/**
 * 加载搜索建议（对应 Java 原版 getSuggest）
 */
private fun loadSuggestWords(keyword: String, onResult: (List<String>) -> Unit) {
    val url = "https://suggest.video.iqiyi.com/?if=mobile&key=${URLEncoder.encode(keyword)}"
    OkHttp.newCall(url).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // 失败时清空建议
            com.fongmi.android.tv.App.post { onResult(emptyList()) }
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                val result = response.body?.string() ?: return
                if (result.isNotEmpty()) {
                    // 解析建议结果
                    val suggestions = parseSuggestions(result)
                    com.fongmi.android.tv.App.post { onResult(suggestions) }
                } else {
                    com.fongmi.android.tv.App.post { onResult(emptyList()) }
                }
            } catch (_: Exception) {
                com.fongmi.android.tv.App.post { onResult(emptyList()) }
            }
        }
    })
}

/**
 * 解析搜索建议结果
 */
private fun parseSuggestions(result: String): List<String> {
    return try {
        val json = com.github.catvod.utils.Json.parse(result)
        if (json.isJsonObject) {
            val data = json.asJsonObject.get("data")
            if (data != null && data.isJsonArray) {
                data.asJsonArray.map { it.asString }.take(10)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}
