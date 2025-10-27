package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class CancelAppScheduleUseCaseTest {

    private lateinit var repository: ScheduleRepository
    private lateinit var appScheduleManager: AppScheduleManager
    private lateinit var useCase: CancelAppScheduleUseCase

    @Before
    fun setup() {
        repository = mock()
        appScheduleManager = mock()
        useCase = CancelAppScheduleUseCase(repository, appScheduleManager)
    }

    @Test
    fun `invoke should return success when schedule is cancelled successfully`(): Unit =
        runBlocking {
            val scheduleId = 1L
            val newStatus = "CANCELLED"

            val result = useCase(scheduleId, newStatus)

            assertTrue(result.isSuccess)
            verify(repository).cancelSchedule(scheduleId, newStatus)
            verify(appScheduleManager).cancelAppSchedule(scheduleId)
        }

}