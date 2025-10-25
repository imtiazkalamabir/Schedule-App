package com.challenge.scheduleapp.domain.model

sealed class ProcessResult {
    data class Success(val message: String) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}