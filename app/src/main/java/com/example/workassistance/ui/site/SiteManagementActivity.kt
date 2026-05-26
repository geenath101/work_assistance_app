package com.example.workassistance.ui.site

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.ui.theme.WorkAssistanceTheme

class SiteManagementActivity : ComponentActivity() {

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
                SiteManagementScreen(
                    site = site,
                    onBack = { finish() },
                    onOpenAttendance = {
                        startActivity(Intent(this, SiteDetailActivity::class.java).apply {
                            putSiteExtras(site)
                        })
                    },
                    onOpenProofOfWork = {
                        startActivity(Intent(this, ProofOfWorkActivity::class.java).apply {
                            putSiteExtras(site)
                        })
                    },
                    onOpenRequests = {
                        startActivity(Intent(this, SiteRequestsActivity::class.java).apply {
                            putSiteExtras(site)
                        })
                    }
                )
            }
        }
    }
}

private fun Intent.putSiteExtras(site: SiteAssignment) {
    putExtra(SiteDetailActivity.EXTRA_ASSIGNMENT_ID, site.assignmentId)
    putExtra(SiteDetailActivity.EXTRA_SITE_ID, site.siteId)
    putExtra(SiteDetailActivity.EXTRA_SITE_NAME, site.siteName)
    putExtra(SiteDetailActivity.EXTRA_SITE_ADDRESS, site.siteAddress)
    putExtra(SiteDetailActivity.EXTRA_SITE_LAT, site.latitude)
    putExtra(SiteDetailActivity.EXTRA_SITE_LNG, site.longitude)
    putExtra(SiteDetailActivity.EXTRA_SITE_RADIUS, site.radiusMeters)
    putExtra(SiteDetailActivity.EXTRA_SIGN_IN_EXPIRY_MINUTES, site.signInExpiryMinutes)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteManagementScreen(
    site: SiteAssignment,
    onBack: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenProofOfWork: () -> Unit,
    onOpenRequests: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(site.siteName.ifBlank { "Site" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = site.siteAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SiteManagementCard(
                title = "Sign In / Sign Out",
                subtitle = "Map + geofence enforced attendance",
                icon = Icons.Default.LocationOn,
                onClick = onOpenAttendance
            )
            SiteManagementCard(
                title = "Proof of Work",
                subtitle = "Upload photos and notes for this site",
                icon = Icons.Default.CameraAlt,
                onClick = onOpenProofOfWork
            )
            SiteManagementCard(
                title = "Make a Request",
                subtitle = "Create a consumables/tools request",
                icon = Icons.AutoMirrored.Filled.Assignment,
                onClick = onOpenRequests
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Saved locally for now. Remote APIs will be wired later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SiteManagementCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
