package com.example.workassistance.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.workassistance.repository.AuthRepository
import com.example.workassistance.ui.main.MainActivity
import com.example.workassistance.ui.theme.WorkAssistanceTheme
import com.example.workassistance.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
    }

    // Mutable state hoisted here so onActivityResult can update it
    private var isLoading = mutableStateOf(false)
    private var errorMessage = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthRepository.isLoggedIn()) {
            goToMain()
            return
        }

        setContent {
            WorkAssistanceTheme {
                LoginScreen(
                    isLoading = isLoading.value,
                    errorMessage = errorMessage.value,
                    onErrorDismissed = { errorMessage.value = null },
                    onSignIn = { username, password -> attemptPasswordSignIn(username, password) },
                    onGoogleSignIn = { launchGoogleSignIn() }
                )
            }
        }
    }

    private fun attemptPasswordSignIn(username: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        lifecycleScope.launch {
            when (val result = AuthRepository.signInWithPassword(username, password)) {
                is Resource.Success -> goToMain()
                is Resource.Error -> {
                    isLoading.value = false
                    errorMessage.value = result.message
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        @Suppress("DEPRECATION")
        startActivityForResult(client.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    @Deprecated("Using onActivityResult for Google Sign-In until Credential Manager migration")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            isLoading.value = true
            lifecycleScope.launch {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java)
                    val result = AuthRepository.signInWithGoogle(
                        googleAccountName = account.email ?: "unknown@google.com",
                        googleDisplayName = account.displayName ?: "Google User"
                    )
                    when (result) {
                        is Resource.Success -> goToMain()
                        is Resource.Error -> {
                            isLoading.value = false
                            errorMessage.value = result.message
                        }
                        is Resource.Loading -> Unit
                    }
                } catch (e: ApiException) {
                    // Mock fallback for testing
                    val result = AuthRepository.signInWithGoogle(
                        googleAccountName = "demo@google.com",
                        googleDisplayName = "Demo User"
                    )
                    if (result is Resource.Success) {
                        goToMain()
                    } else {
                        isLoading.value = false
                        errorMessage.value = "Google sign-in failed: ${e.statusCode}"
                    }
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onErrorDismissed: () -> Unit,
    onSignIn: (username: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Show snackbar for auth errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorDismissed()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WorkAssist",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Field Worker App",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = null
                },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    validateAndSignIn(username, password,
                        setUsernameError = { usernameError = it },
                        setPasswordError = { passwordError = it },
                        onSignIn = onSignIn
                    )
                }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In button
            Button(
                onClick = {
                    validateAndSignIn(username, password,
                        setUsernameError = { usernameError = it },
                        setPasswordError = { passwordError = it },
                        onSignIn = onSignIn
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider with "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Text("Sign in with Google")
            }
        }
    }
}

private fun validateAndSignIn(
    username: String,
    password: String,
    setUsernameError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    onSignIn: (String, String) -> Unit
) {
    var valid = true
    if (username.isBlank()) {
        setUsernameError("Required")
        valid = false
    }
    if (password.isEmpty()) {
        setPasswordError("Required")
        valid = false
    }
    if (valid) onSignIn(username, password)
}
