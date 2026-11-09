package com.example.minimallauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.minimallauncher.data.repository.FavoriteAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoriteAppsViewModel(
    application: Application
): AndroidViewModel(application) {

    private val repository = FavoriteAppsRepository(application)
    private val _favorites = MutableStateFlow(
        repository.getFavorites()
    )
    val favorites: StateFlow<List<String>> =
        _favorites.asStateFlow()

    fun toggleFavorite(packageName: String) {
        val currentFavorites = _favorites.value
        val updatedFavorites = if (packageName in currentFavorites) {
            currentFavorites - packageName
        } else {
            if (currentFavorites.size >= 5) {
                return
            }
            currentFavorites + packageName
        }
        _favorites.value = updatedFavorites
        repository.saveFavorites(updatedFavorites)
    }
}