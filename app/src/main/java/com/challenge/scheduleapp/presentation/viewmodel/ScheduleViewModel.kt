package com.challenge.scheduleapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.model.ProcessResult
import com.challenge.scheduleapp.domain.usecase.AddAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.GetAllAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.GetInstalledAppsUseCase
import com.challenge.scheduleapp.presentation.model.ScheduleListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val addAppScheduleUseCase: AddAppScheduleUseCase,
    private val getAllAppScheduleUseCase: GetAllAppScheduleUseCase
) : ViewModel() {

    private val _appSchedulesUiState = MutableLiveData<ScheduleListUiState>()
    val appSchedulesUiState: LiveData<ScheduleListUiState> = _appSchedulesUiState


    private val _installedApps = MutableLiveData<List<InstalledApp>>()
    val installedApps: LiveData<List<InstalledApp>> = _installedApps

    private val _processResult = MutableLiveData<ProcessResult?>()
    val processResult: LiveData<ProcessResult?> = _processResult

    init {
        loadAppSchedules()
    }

    private fun loadAppSchedules() {
        viewModelScope.launch {
            _appSchedulesUiState.value = ScheduleListUiState(isLoading = true)

            getAllAppScheduleUseCase()
                .distinctUntilChanged()
                .catch { e ->
                    _appSchedulesUiState.postValue( ScheduleListUiState(error = "Error loading schedules"))
                    Log.e(TAG, "Error loading schedules", e)
                }
                .collect { schedules ->
                _appSchedulesUiState.value = ScheduleListUiState(schedules = schedules)
            }
        }
    }

    fun loadInstalledApps() {

        viewModelScope.launch {
            try {
                val apps = getInstalledAppsUseCase()
                _installedApps.postValue(apps)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading installed apps: ${e.message}")
            } finally {
            }
        }

    }

    fun addAppSchedule(packageName: String, appName: String, scheduledTime: Long) {
        Log.d(TAG, "addAppSchedule: $packageName, $appName, $scheduledTime")
        viewModelScope.launch {
            val result = addAppScheduleUseCase(packageName, appName, scheduledTime)
            result.onSuccess { scheduleId ->
                Log.d(TAG, "Schedule added to database successfully with id: $scheduleId")

                // Schedule the alarm for the selected time

                _processResult.postValue(ProcessResult.Success("Schedule added successfully"))

            }.onFailure { e ->
                _processResult.postValue(ProcessResult.Error("Failed to add scheduled"))
                Log.e(TAG, "Error adding schedule: ${e.message}")
            }
        }
    }

    fun clearProcessResult() {
        _processResult.value = null
    }


    companion object {
        private const val TAG = "ScheduleViewModel"
    }

}

