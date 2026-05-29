package com.example.workassistance.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workassistance.repository.AuthRepository

private enum class ProfileSection { Menu, EmployeeDetails, PaymentHistory, HrRequests }

@Composable
fun ProfileScreen() {
    val user = AuthRepository.getCurrentUser()

    var section by remember { mutableStateOf(ProfileSection.Menu) }

    when (section) {
        ProfileSection.Menu -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Profile", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = user?.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                ElevatedCard(
                    onClick = { section = ProfileSection.EmployeeDetails },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Employee details") },
                        supportingContent = { Text("View your profile information") },
                        leadingContent = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                        }
                    )
                }

                ElevatedCard(
                    onClick = { section = ProfileSection.PaymentHistory },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Payment history") },
                        supportingContent = { Text("TODO") },
                        leadingContent = {
                            Icon(Icons.Default.Payments, contentDescription = null)
                        }
                    )
                }

                ElevatedCard(
                    onClick = { section = ProfileSection.HrRequests },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("HR requests") },
                        supportingContent = { Text("TODO") },
                        leadingContent = {
                            Icon(Icons.Default.SupportAgent, contentDescription = null)
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        ProfileSection.EmployeeDetails -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeader(title = "Employee details", onBack = { section = ProfileSection.Menu })
                Spacer(modifier = Modifier.height(20.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!user?.email.isNullOrBlank()) {
                    ProfileInfoRow(
                        icon = { Icon(Icons.Default.Email, contentDescription = null) },
                        label = "Email",
                        value = user?.email ?: ""
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                if (!user?.companyName.isNullOrBlank()) {
                    ProfileInfoRow(
                        icon = { Icon(Icons.Default.Business, contentDescription = null) },
                        label = "Company",
                        value = user?.companyName ?: ""
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        ProfileSection.PaymentHistory -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ProfileHeader(title = "Payment history", onBack = { section = ProfileSection.Menu })
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "TODO: Payment history screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ProfileSection.HrRequests -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ProfileHeader(title = "HR requests", onBack = { section = ProfileSection.Menu })
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "TODO: HR requests screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ProfileInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
