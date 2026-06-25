package com.fongmi.android.tv.ui.compose.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Site

/**
 * 站点切换对话框
 * 对应 SiteDialog
 * 支持长按全选/全取消功能
 */
@Composable
fun SiteDialog(
    sites: List<Site>,
    search: Boolean = false,
    change: Boolean = false,
    onSiteClick: (Site) -> Unit,
    onSearchToggle: (Int, Site) -> Unit,
    onChangeToggle: (Int, Site) -> Unit,
    onSearchLongClick: ((List<Site>) -> Unit)? = null,
    onChangeLongClick: ((List<Site>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "站点选择",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(sites) { index, site ->
                    SiteItem(
                        site = site,
                        search = search,
                        change = change,
                        onClick = { onSiteClick(site) },
                        onSearchToggle = { onSearchToggle(index, site) },
                        onChangeToggle = { onChangeToggle(index, site) },
                        onSearchLongClick = {
                            // 长按搜索图标：全选/全取消所有站点的搜索属性
                            // 对应 Java 原版 onSearchLongClick
                            onSearchLongClick?.invoke(sites)
                        },
                        onChangeLongClick = {
                            // 长按切换图标：全选/全取消所有站点的切换属性
                            // 对应 Java 原版 onChangeLongClick
                            onChangeLongClick?.invoke(sites)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SiteItem(
    site: Site,
    search: Boolean,
    change: Boolean,
    onClick: () -> Unit,
    onSearchToggle: () -> Unit,
    onChangeToggle: () -> Unit,
    onSearchLongClick: () -> Unit,
    onChangeLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = site.getName(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        if (search) {
            Checkbox(
                checked = site.isSearchable(),
                onCheckedChange = { onSearchToggle() },
                modifier = Modifier.combinedClickable(
                    onClick = { onSearchToggle() },
                    onLongClick = { onSearchLongClick() }
                )
            )
        }

        if (change) {
            Checkbox(
                checked = site.isChangeable(),
                onCheckedChange = { onChangeToggle() },
                modifier = Modifier.combinedClickable(
                    onClick = { onChangeToggle() },
                    onLongClick = { onChangeLongClick() }
                )
            )
        }
    }
}
