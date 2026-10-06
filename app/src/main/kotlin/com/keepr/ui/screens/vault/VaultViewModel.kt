package com.keepr.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.data.model.VaultEntry
import com.keepr.data.repository.SortOrder
import com.keepr.data.repository.VaultRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

private data class VaultInputs(
    val rawQuery: String,
    val query: String,
    val category: String?,
    val sort: SortOrder
)

data class VaultState(
    val entries: List<VaultEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val sortOrder = MutableStateFlow(SortOrder.NAME_ASC)

    private val debouncedSearchQuery = searchQuery
        .debounce(120)
        .distinctUntilChanged()

    val state: StateFlow<VaultState> = combine(
        searchQuery,
        debouncedSearchQuery,
        selectedCategory,
        sortOrder,
        repository.observeEntries()
    ) { rawQuery, query, category, sort, entries ->
        VaultInputs(rawQuery, query, category, sort) to entries
    }
        .map { (inputs, entries) ->
            val query = inputs.query.trim()
            val filtered = entries.asSequence()
                .filter { entry ->
                    val categoryMatches = inputs.category == null || entry.category == inputs.category
                    val searchMatches = query.isBlank() || entry.matchesQuery(query)
                    categoryMatches && searchMatches
                }
                .toList()

            val sorted = when (inputs.sort) {
                SortOrder.NAME_ASC -> filtered.sortedWith(favoriteFirstThen(compareBy { it.serviceName.lowercase(Locale.ROOT) }))
                SortOrder.NAME_DESC -> filtered.sortedWith(favoriteFirstThen(compareByDescending { it.serviceName.lowercase(Locale.ROOT) }))
                SortOrder.RECENT -> filtered.sortedWith(favoriteFirstThen(compareByDescending { it.updatedAt }))
                SortOrder.CREATED -> filtered.sortedWith(favoriteFirstThen(compareByDescending { it.createdAt }))
            }

            VaultState(
                entries = sorted,
                searchQuery = inputs.rawQuery,
                selectedCategory = inputs.category,
                sortOrder = inputs.sort
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultState()
        )

    fun onSearchQuery(query: String) = searchQuery.update { query }

    fun onCategorySelected(category: String?) = selectedCategory.update { category }

    fun onSortOrderChanged(order: SortOrder) = sortOrder.update { order }

    fun toggleFavorite(entry: VaultEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }

    private fun VaultEntry.matchesQuery(query: String): Boolean {
        return serviceName.contains(query, ignoreCase = true) ||
            accountLabel.contains(query, ignoreCase = true) ||
            username.contains(query, ignoreCase = true) ||
            email.contains(query, ignoreCase = true) ||
            website.contains(query, ignoreCase = true) ||
            category.contains(query, ignoreCase = true) ||
            notes.contains(query, ignoreCase = true)
    }

    private fun favoriteFirstThen(comparator: Comparator<VaultEntry>): Comparator<VaultEntry> =
        compareByDescending<VaultEntry> { it.isFavorite }.then(comparator)
}
