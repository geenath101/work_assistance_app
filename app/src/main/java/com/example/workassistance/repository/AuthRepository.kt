package com.example.workassistance.repository

import com.example.workassistance.data.model.User
import com.example.workassistance.util.Resource
import kotlinx.coroutines.delay

/**
 * Mock authentication repository.
 * Replace the bodies of signInWithPassword() and signInWithGoogle() with real
 * backend / Firebase / Google Identity calls when the backend is ready.
 *
 * A signed-in user is kept in memory for the session. In a real app this would
 * be persisted (SharedPreferences / DataStore / token store).
 */
object AuthRepository {

    private var currentUser: User? = null

    fun getCurrentUser(): User? = currentUser

    fun isLoggedIn(): Boolean = currentUser != null

    fun signOut() {
        currentUser = null
    }

    /**
     * Mock username/password sign-in.
     * Accepts any non-empty credentials and always succeeds after a short delay.
     * TODO: replace with real API call.
     */
    suspend fun signInWithPassword(username: String, password: String): Resource<User> {
        delay(1000) // simulate network latency
        return if (username.isNotBlank() && password.isNotBlank()) {
            val user = User(
                id = "mock-user-001",
                // TODO: replace with the real employee UUID returned by the backend auth API
                employeeId = "c3d4e5f6-a7b8-9012-cdef-123456789012",
                displayName = username.trim(),
                email = "$username@example.com",
                companyName = "WorkAssist Co."
            )
            currentUser = user
            Resource.Success(user)
        } else {
            Resource.Error("Username and password cannot be empty.")
        }
    }

    /**
     * Mock Google sign-in.
     * In production replace with the Google Sign-In SDK result processing.
     * TODO: replace with real Google credential handling.
     */
    suspend fun signInWithGoogle(googleAccountName: String, googleDisplayName: String): Resource<User> {
        delay(800)
        val user = User(
            id = "google-user-001",
            // TODO: replace with the real employee UUID returned by the backend after Google auth
            employeeId = "457124e9",
            displayName = googleDisplayName,
            email = googleAccountName,
            companyName = "WorkAssist Co."
        )
        currentUser = user
        return Resource.Success(user)
    }
}
