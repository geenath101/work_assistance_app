package com.example.workassistance.ui.site

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.ui.theme.WorkAssistanceTheme

class SiteTasksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val site = SiteAssignment(
            assignmentId = intent.getStringExtra(SiteDetailActivity.EXTRA_ASSIGNMENT_ID) ?: "",
            siteId = intent.getStringExtra(SiteDetailActivity.EXTRA_SITE_ID) ?: "",
            siteName = intent.getStringExtra(SiteDetailActivity.EXTRA_SITE_NAME) ?: "",
            siteAddress = intent.getStringExtra(SiteDetailActivity.EXTRA_SITE_ADDRESS) ?: "",
            latitude = intent.getDoubleExtra(SiteDetailActivity.EXTRA_SITE_LAT, 0.0),
            longitude = intent.getDoubleExtra(SiteDetailActivity.EXTRA_SITE_LNG, 0.0),
            radiusMeters = intent.getDoubleExtra(SiteDetailActivity.EXTRA_SITE_RADIUS, 100.0),
            employeeId = "",
            assignedAt = "",
            active = true,
            signInExpiryMinutes = intent.getIntExtra(SiteDetailActivity.EXTRA_SIGN_IN_EXPIRY_MINUTES, 12 * 60)
        )

        setContent {
            WorkAssistanceTheme {
                SiteTasksScreen(site = site, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteTasksScreen(
    site: SiteAssignment,
    onBack: () -> Unit
) {
    // Placeholder screen: remote API not wired yet.
    // When backend endpoint is available, this will show the fetched tasks list.
    LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Assigned Tasks") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = site.siteName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = site.siteAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks to show yet (API not connected)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
