package com.example.minimallauncher.launcher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimallauncher.data.repository.AppRepository
import com.example.minimallauncher.core.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application){
    private val repository = AppRepository(application)
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    init {
        refreshApps()
    }
    fun refreshApps() {
        viewModelScope.launch {
            _apps.value = withContext(Dispatchers.IO) {
                repository.getInstalledApps()
            }
        }
    }

}