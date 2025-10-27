package com.challenge.scheduleapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.model.ProcessResult
import com.challenge.scheduleapp.domain.usecase.AddAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.CancelAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.DeleteAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.GetAllAppScheduleUseCase
import com.challenge.scheduleapp.domain.usecase.GetInstalledAppsUseCase
import com.challenge.scheduleapp.domain.usecase.UpdateAppScheduleUseCase
import com.challenge.scheduleapp.presentation.model.ScheduleListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val addAppScheduleUseCase: AddAppScheduleUseCase,
    private val getAllAppScheduleUseCase: GetAllAppScheduleUseCase,
    private val cancelAppScheduleUseCase: CancelAppScheduleUseCase,
    private val deleteAppScheduleUseCase: DeleteAppScheduleUseCase,
    private val updateAppScheduleUseCase: UpdateAppScheduleUseCase
) : ViewModel() {

    private val _appSchedulesUiState = MutableLiveData<ScheduleListUiState>()
    val appSchedulesUiState: LiveData<ScheduleListUiState> = _appSchedulesUiState


    private val _installedApps = MutableLiveData<List<InstalledApp>>()
    val installedApps: LiveData<List<InstalledApp>> = _installedApps

    private val _processResult = MutableLiveData<ProcessResult?>()
    val processResult: LiveData<ProcessResult?> = _processResult

    private var isLoadingApps = false

    init {
        loadAppSchedules()
    }

    private fun loadAppSchedules() {
        viewModelScope.launch {
            _appSchedulesUiState.value = ScheduleListUiState(isLoading = true)

            getAllAppScheduleUseCase()
                .flowOn(Dispatchers.IO)
                .debounce(300) // waiting here for 300ms before emitting new value to avoid rapid updates
                .distinctUntilChanged()
                .catch { e ->
                    _appSchedulesUiState.postValue(ScheduleListUiState(error = "Error loading schedules"))
                    Log.e(TAG, "Error loading schedules", e)
                }
                .collect { schedules ->
                    _appSchedulesUiState.postValue(ScheduleListUiState(schedules = schedules))
                }
        }
    }

    fun loadInstalledApps() {

        // Avoiding loading if already loaded or during loading state
        if (isLoadingApps || !_installedApps.value.isNullOrEmpty()) {
            return
        }

        isLoadingApps = true

        viewModelScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    getInstalledAppsUseCase()
                }
                _installedApps.postValue(apps)
            } catch (e: Exception) {
                _processResult.postValue(
                    ProcessResult.Error("Failed to load installed apps")
                )
                Log.e(TAG, "Error loading installed apps: ${e.message}")
            } finally {
                isLoadingApps = false
            }
        }

    }

    fun addAppSchedule(packageName: String, appName: String, scheduledTime: Long) {
        Log.d(TAG, "addAppSchedule: $packageName, $appName, $scheduledTime")
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                addAppScheduleUseCase(packageName, appName, scheduledTime)
            }
            result.onSuccess { scheduleId ->
                Log.d(TAG, "Schedule added to database successfully with id: $scheduleId")
                _processResult.postValue(ProcessResult.Success("Schedule added successfully"))
            }.onFailure { e ->
                if (e.message.equals("Time conflicting with another schedule") || e.message.equals("The scheduled time must be in the future")) {
                    _processResult.postValue(ProcessResult.Error("Failed: ${e.message}"))
                } else {
                    _processResult.postValue(ProcessResult.Error("Failed to add scheduled"))
                }
                Log.e(TAG, "Error adding schedule: ${e.message}")
            }
        }
    }

    fun cancelAppSchedule(scheduleId: Long, newStatus: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                cancelAppScheduleUseCase(scheduleId, newStatus)
            }
            result.onSuccess {
                _processResult.postValue(ProcessResult.Success("Schedule cancelled successfully"))
                Log.d(TAG, "Schedule cancelled successfully")
            }.onFailure { e ->
                _processResult.postValue(ProcessResult.Error("Failed to cancel schedule"))
                Log.e(TAG, "Error canceling schedule: ${e.message}")
            }
        }
    }

    fun deleteAppSchedule(scheduleId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteAppScheduleUseCase(scheduleId)
                }
                _processResult.postValue(ProcessResult.Success("Schedule deleted successfully"))
                Log.d(TAG, "Schedule deleted successfully")
            } catch (e: Exception) {
                _processResult.postValue(ProcessResult.Error("Failed to delete schedule"))
                Log.e(TAG, "Error deleting schedule: ${e.message}")
            }
        }
    }

    fun updateAppSchedule(scheduleId: Long, newScheduledTime: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                updateAppScheduleUseCase(scheduleId, newScheduledTime)
            }
            result.onSuccess {
                _processResult.postValue(ProcessResult.Success("Schedule updated successfully"))
                Log.d(TAG, "Schedule updated successfully")
            }.onFailure { e ->
                if (e.message.equals("Time conflicting with another schedule") || e.message.equals("The scheduled time must be in the future")) {
                    _processResult.postValue(ProcessResult.Error("Failed: ${e.message}"))
                } else {
                    _processResult.postValue(ProcessResult.Error("Failed to update schedule"))
                }
                Log.e(TAG, "Error updating schedule: ${e.message}")
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

