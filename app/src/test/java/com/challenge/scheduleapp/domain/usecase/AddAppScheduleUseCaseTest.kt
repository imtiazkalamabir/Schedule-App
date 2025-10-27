package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AddAppScheduleUseCaseTest {

    private lateinit var repository: ScheduleRepository
    private lateinit var appScheduleManager: AppScheduleManager
    private lateinit var useCase: AddAppScheduleUseCase

    @Before
    fun setup() {
        repository = mock()
        appScheduleManager = mock()
        useCase = AddAppScheduleUseCase(repository, appScheduleManager)
    }

    @Test
    fun `invoke should return success when schedule is added successfully`(): Unit = runBlocking {
        val packageName = "com.example.app"
        val appName = "Example App"
        val scheduledTime = System.currentTimeMillis() + 3600000
        val scheduleId = 1L

        whenever(repository.hasTimeConflict(scheduledTime)).thenReturn(false)
        whenever(repository.insertSchedule(any())).thenReturn(scheduleId)


        val result = useCase(packageName, appName, scheduledTime)

        assertTrue(result.isSuccess)
        assertEquals(scheduleId, result.getOrNull())
        verify(repository).hasTimeConflict(scheduledTime)
        verify(repository).insertSchedule(any())
        verify(appScheduleManager).scheduleApp(scheduleId, packageName, scheduledTime)
    }

    @Test
    fun `invoke should return failure when time conflict occurs`(): Unit = runBlocking {
        val packageName = "com.example.app"
        val appName = "Example App"
        val scheduledTime = System.currentTimeMillis() + 3600000

        whenever(repository.hasTimeConflict(scheduledTime)).thenReturn(true)

        val result = useCase(packageName, appName, scheduledTime)

        assertTrue(result.isFailure)
        assertEquals("Time conflicting with another schedule", result.exceptionOrNull()?.message)
        verify(repository).hasTimeConflict(scheduledTime)
        verify(repository, never()).insertSchedule(any())
    }


}

