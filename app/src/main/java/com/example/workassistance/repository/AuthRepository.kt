package com.example.workassistance.repository

import com.example.workassistance.data.model.User
import com.example.workassistance.data.remote.api.RetrofitClient
import com.example.workassistance.data.remote.model.LoginRequest
import com.example.workassistance.util.Resource

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
    private var accessToken: String? = null

    fun getCurrentUser(): User? = currentUser

    fun isLoggedIn(): Boolean = currentUser != null

    fun signOut() {
        currentUser = null
        accessToken = null
    }

    /**
     * Mock username/password sign-in.
     * Accepts any non-empty credentials and always succeeds after a short delay.
     * Calls POST /auth/login.
     */
    suspend fun signInWithPassword(username: String, password: String): Resource<User> {
        if (username.isBlank() || password.isBlank()) {
            return Resource.Error("Username and password cannot be empty.")
        }

        return try {
            // Company is currently required by the API payload. Keep it constant for now.
            val request = LoginRequest(
                userName = username.trim(),
                password = password,
                company = "WorkAssist Co."
            )
            val response = RetrofitClient.apiService.login(request)
            if (!response.isSuccessful || response.body() == null) {
                return Resource.Error("Login failed: ${response.code()} ${response.message()}")
            }

            val body = response.body()!!
            accessToken = body.accessToken

            val remoteUser = body.user
                ?: return Resource.Error("Login failed: missing user payload")

            val user = User(
                id = remoteUser.id,
                // App uses employeeId for site assignments.
                // Backend login user.id currently matches the employee identifier.
                employeeId = remoteUser.id,
                displayName = remoteUser.username,
                email = "${remoteUser.username}@example.com",
                companyName = "WorkAssist Co."
            )

            currentUser = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Mock Google sign-in.
     * In production replace with the Google Sign-In SDK result processing.
     * TODO: replace with real Google credential handling.
     */
    suspend fun signInWithGoogle(googleAccountName: String, googleDisplayName: String): Resource<User> {
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

    fun getAccessToken(): String? = accessToken
}
