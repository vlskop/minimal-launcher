package com.example.minimallauncher.launcher.home

data class HomeUiState(
    val time: String = "12:00",
    val date: String = "Monday, November 8",
    val favoriteApps: List<String> = listOf(
        "Phone",
        "Messages",
        "Browser",
        "Camera",
        "Settings"
    )
)