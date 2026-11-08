package com.example.minimallauncher.launcher.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    private val dateFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

    init {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()

                _uiState.value = _uiState.value.copy(
                    time = now.format(timeFormatter),
                    date = now.format(dateFormatter)
                )

                delay(1_000.milliseconds)
            }
        }
    }
}