package com.example.workassistance.data.remote.api

import com.example.workassistance.data.remote.model.AttendanceRequest
import com.example.workassistance.data.remote.model.AttendanceResponse
import com.example.workassistance.data.remote.model.CompleteTaskRequest
import com.example.workassistance.data.remote.model.CompleteTaskResponse
import com.example.workassistance.data.remote.model.LoginRequest
import com.example.workassistance.data.remote.model.LoginResponse
import com.example.workassistance.data.remote.model.ShiftSlot
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.data.remote.model.SiteTaskDto
import kotlinx.coroutines.delay
import retrofit2.Response
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory backend mock. Keeps task completion state for the app session.
 * This is intentionally simple; once real endpoints are ready, swap RetrofitClient.apiService.
 */
class MockApiService : ApiService {

    private val tasksBySite: MutableMap<String, MutableList<SiteTaskDto>> = ConcurrentHashMap()

    override suspend fun getSitesByEmployee(employeeId: String): Response<List<SiteAssignment>> {
        delay(250)
        // Minimal sample sites; real app already uses this endpoint, but keep mock functional.
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val dow = now.dayOfWeek
        val shift = ShiftSlot(
            day = dow.name.lowercase().replaceFirstChar { it.uppercase() },
            shiftStart = "09:00",
            shiftEnd = "17:00",
            allocatedHours = 8
        )
        val sites = listOf(
            SiteAssignment(
                assignmentId = UUID.randomUUID().toString(),
                siteId = "site-001",
                siteName = "Warehouse A",
                siteAddress = "12 Example St",
                latitude = -37.8136,
                longitude = 144.9631,
                radiusMeters = 150.0,
                employeeId = employeeId,
                assignedAt = now.toInstant().toString(),
                active = true,
                signInExpiryMinutes = 12 * 60,
                shiftSlots = listOf(shift)
            ),
            SiteAssignment(
                assignmentId = UUID.randomUUID().toString(),
                siteId = "site-002",
                siteName = "Office B",
                siteAddress = "99 Sample Ave",
                latitude = -37.8150,
                longitude = 144.9660,
                radiusMeters = 100.0,
                employeeId = employeeId,
                assignedAt = now.toInstant().toString(),
                active = true,
                signInExpiryMinutes = 12 * 60,
                shiftSlots = listOf(shift)
            )
        )
        return Response.success(sites)
    }

    override suspend fun postAttendance(request: AttendanceRequest): Response<AttendanceResponse> {
        delay(150)
        return Response.success(
            AttendanceResponse(
                success = true,
                message = "Recorded ${request.eventType} for ${request.siteName}"
            )
        )
    }

    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        delay(200)
        return Response.success(
            LoginResponse(
                accessToken = "mock-token",
                tokenType = "Bearer",
                expiresAt = null,
                user = null
            )
        )
    }

    override suspend fun getTasksBySite(siteId: String): Response<List<SiteTaskDto>> {
        delay(250)
        val existing = tasksBySite[siteId]
        if (existing != null) return Response.success(existing.toList())

        val now = System.currentTimeMillis()
        val seeded = mutableListOf(
            SiteTaskDto(
                id = "task-${siteId}-001",
                siteId = siteId,
                title = "Safety checklist",
                description = "Complete the daily safety checklist and attach a photo of the signed sheet.",
                createdAt = now - 86_400_000L,
                dueAt = now + 6 * 60 * 60 * 1000L,
                priority = "HIGH",
                isCompleted = false,
                completedAt = null
            ),
            SiteTaskDto(
                id = "task-${siteId}-002",
                siteId = siteId,
                title = "Area clean-up",
                description = "Clean the designated area and attach before/after photos.",
                createdAt = now - 3_600_000L,
                dueAt = null,
                priority = "NORMAL",
                isCompleted = false,
                completedAt = null
            )
        )
        tasksBySite[siteId] = seeded
        return Response.success(seeded.toList())
    }

    override suspend fun completeTask(request: CompleteTaskRequest): Response<CompleteTaskResponse> {
        delay(250)
        if (request.proofPhotoUris.isEmpty()) {
            return Response.success(CompleteTaskResponse(success = false, message = "Proof photo required"))
        }

        val taskId = request.taskId
        val updated = tasksBySite.values.firstNotNullOfOrNull { list ->
            val idx = list.indexOfFirst { it.id == taskId }
            if (idx < 0) return@firstNotNullOfOrNull null
            val t = list[idx]
            val now = System.currentTimeMillis()
            val newT = t.copy(isCompleted = true, completedAt = now)
            list[idx] = newT
            newT
        }

        return if (updated != null) {
            Response.success(CompleteTaskResponse(success = true, message = "Task completed"))
        } else {
            Response.success(CompleteTaskResponse(success = false, message = "Task not found"))
        }
    }
}
