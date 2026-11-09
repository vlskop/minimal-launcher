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

    fun moveFavorite(fromIndex: Int, toIndex: Int) {
        val currentFavorites = _favorites.value

        if (fromIndex !in currentFavorites.indices ||
            toIndex !in currentFavorites.indices
            ){
            return
        }
        if (fromIndex == toIndex) {
            return
        }

        val updatedFavorites = currentFavorites.toMutableList()
        val movedApp = updatedFavorites.removeAt(fromIndex)
        updatedFavorites.add(toIndex, movedApp)

        _favorites.value = updatedFavorites
        repository.saveFavorites(updatedFavorites)

    }
}