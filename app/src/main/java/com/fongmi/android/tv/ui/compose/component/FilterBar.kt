package com.fongmi.android.tv.ui.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.bean.Value

/**
 * 筛选项数据类
 */
data class FilterItem(
    val key: String,
    val value: String,
    val isSelected: Boolean = false
)

/**
 * 筛选栏组件
 * 对应现有 FlexboxLayout + ValueAdapter
 * 使用 FlowRow 实现换行布局（对应 Java 原版 FlexboxLayout）
 * 支持单选逻辑（对应 Java 原版 ValueAdapter.onItemClick）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    filters: List<FilterItem>,
    onFilterSelected: (FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // 内部维护选中状态，实现单选逻辑
    var selectedFilter by remember { mutableStateOf(filters.firstOrNull { it.isSelected }) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter.key == selectedFilter?.key && filter.value == selectedFilter?.value

            FilterChip(
                selected = isSelected,
                onClick = {
                    // 单选逻辑：点击后更新选中状态
                    selectedFilter = filter
                    onFilterSelected(filter)
                },
                label = {
                    Text(
                        text = filter.value,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * 筛选栏组件（使用 Value 对象）
 * 对应 Java 原版 ValueAdapter
 * 使用 FlowRow 实现换行布局（对应 Java 原版 FlexboxLayout）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBarWithValue(
    key: String,
    values: List<Value>,
    onFilterSelected: (String, Value) -> Unit,
    modifier: Modifier = Modifier
) {
    // 内部维护选中状态，实现单选逻辑
    var selectedValue by remember { mutableStateOf(values.firstOrNull { it.isSelected }) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        values.forEach { value ->
            val isSelected = value == selectedValue

            FilterChip(
                selected = isSelected,
                onClick = {
                    // 单选逻辑：点击后更新选中状态（对应 Java 原版 ValueAdapter.onItemClick）
                    // for (Value item : mItems) item.setSelected(value);
                    values.forEach { it.setSelected(value) }
                    selectedValue = value
                    onFilterSelected(key, value)
                },
                label = {
                    Text(
                        text = value.getN(),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * 标签组件
 * 用于显示单个标签
 */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
