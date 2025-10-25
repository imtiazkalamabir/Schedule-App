package com.challenge.scheduleapp.presentation.model

import com.challenge.scheduleapp.domain.model.AppSchedule

data class ScheduleListUiState(
    val schedules: List<AppSchedule> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null

)
