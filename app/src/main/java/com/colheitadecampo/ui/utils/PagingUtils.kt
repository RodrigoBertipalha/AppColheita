package com.colheitadecampo.ui.utils

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

/**
 * Helper function for working with Paging in LazyColumn.
 * Use this when the built-in "items" function is not available due to API versioning.
 */
fun <T : Any> LazyListScope.pagingItems(
    items: LazyPagingItems<T>,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(count = items.itemCount) { index ->
        itemContent(items[index])
    }
}
