package com.example.minimallauncher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class FavoriteAppsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(
        "favorite_apps",
        Context.MODE_PRIVATE
    )

    fun getFavorites(): List<String> {
        val savedFavorites = preferences.getString(
            "favorite_packages",
            ""
        ) ?: ""

        return if (savedFavorites.isEmpty()) {
            emptyList()
        } else {
            savedFavorites.split("|")
        }

    }

    fun saveFavorites(packageNames: List<String>) {
        preferences.edit {
            putString("favorite_packages", packageNames.joinToString("|"))
        }
    }

}