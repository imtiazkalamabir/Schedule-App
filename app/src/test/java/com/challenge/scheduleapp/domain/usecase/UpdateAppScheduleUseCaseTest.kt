package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.ScheduleStatus
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class UpdateAppScheduleUseCaseTest {

    private lateinit var repository: ScheduleRepository

    private lateinit var appManager: AppScheduleManager

    private lateinit var useCase: UpdateAppScheduleUseCase

    @Before
    fun setup() {
        repository = mock()
        appManager = mock()
        useCase = UpdateAppScheduleUseCase(repository, appManager)
    }

    @Test
    fun `invoke should return success when schedule is updated successfully`(): Unit = runBlocking {
        val scheduleId = 1L
        val newScheduledTime = System.currentTimeMillis() + 3600000

        val existingSchedule = AppSchedule(
            id = scheduleId,
            packageName = "com.example.app",
            appName = "Example App",
            scheduledTime = System.currentTimeMillis() + 7200000,
            status = ScheduleStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        whenever(repository.hasTimeConflict(newScheduledTime, scheduleId)).thenReturn(false)
        whenever(repository.getScheduleById(scheduleId)).thenReturn(existingSchedule)

        val result = useCase(scheduleId, newScheduledTime)

        assertTrue(result.isSuccess)
        verify(repository).hasTimeConflict(newScheduledTime, scheduleId)
        verify(repository).getScheduleById(scheduleId)
        verify(repository).updateSchedule(any())
        verify(appManager).updateAppSchedule(
            scheduleId,
            existingSchedule.packageName,
            newScheduledTime
        )
    }

}