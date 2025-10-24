package com.challenge.scheduleapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.usecase.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _installedApps = MutableLiveData<List<InstalledApp>>()
    val installedApps: LiveData<List<InstalledApp>> = _installedApps

    fun loadInstalledApps() {

        viewModelScope.launch {
            try {
                val apps = getInstalledAppsUseCase()
                _installedApps.postValue(apps)
            } catch (e: Exception) {
                Log.d(TAG, "Error loading installed apps: ${e.message}")
            } finally {
            }
        }

    }

    companion object {
        private const val TAG = "ScheduleViewModel"
    }


}

