package com.example.festivalappmobile.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FestivalSortOption {
    LATEST_CREATED, // ID Desc
    DATE_ASC        // DateDebut Asc
}

sealed class FestivalUiState {
    object Loading : FestivalUiState()
    data class Success(
        val festivals: List<Festival>,
        val currentSort: FestivalSortOption = FestivalSortOption.LATEST_CREATED
    ) : FestivalUiState()
    data class Error(val message: String) : FestivalUiState()
}

class FestivalListViewModel(
    private val repository: FestivalRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<FestivalUiState>(FestivalUiState.Loading)
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    private var currentSort = FestivalSortOption.LATEST_CREATED

    init {
        loadSortPreference()
        observeFestivals()
        loadFestivals()
    }

    private fun loadSortPreference() {
        val sortName = sharedPrefs.getString("festival_sort_option", FestivalSortOption.LATEST_CREATED.name)
        currentSort = try {
            FestivalSortOption.valueOf(sortName ?: FestivalSortOption.LATEST_CREATED.name)
        } catch (e: Exception) {
            FestivalSortOption.LATEST_CREATED
        }
    }

    private fun saveSortPreference(option: FestivalSortOption) {
        sharedPrefs.edit().putString("festival_sort_option", option.name).apply()
    }

    private fun observeFestivals() {
        viewModelScope.launch {
            repository.festivals.collect { festivals ->
                applySortAndEmit(festivals)
            }
        }
    }

    private fun applySortAndEmit(festivals: List<Festival>) {
        val sortedList = when (currentSort) {
            FestivalSortOption.LATEST_CREATED -> festivals.sortedByDescending { it.id }
            FestivalSortOption.DATE_ASC -> festivals.sortedBy { it.dateDebut }
        }
        _uiState.value = FestivalUiState.Success(sortedList, currentSort)
    }

    fun setSortOption(option: FestivalSortOption) {
        if (currentSort != option) {
            currentSort = option
            saveSortPreference(option)
            // Re-apply sort to current festivals if we are in Success state
            val currentState = _uiState.value
            if (currentState is FestivalUiState.Success) {
                applySortAndEmit(repository.festivals.value)
            }
        }
    }

    fun loadFestivals() {
        viewModelScope.launch {
            try {
                repository.getFestivals()
            } catch (e: Exception) {
                _uiState.value = FestivalUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deleteFestival(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteFestival(id)
            } catch (e: Exception) {
                // Error handling can be added here if needed
            }
        }
    }


}
