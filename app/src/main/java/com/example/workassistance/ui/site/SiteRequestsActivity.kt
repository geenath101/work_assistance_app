package com.example.workassistance.ui.site

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workassistance.data.local.AppDatabase
import com.example.workassistance.data.local.entity.SiteRequestEntity
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.SiteRequestRepository
import com.example.workassistance.ui.theme.WorkAssistanceTheme
import com.example.workassistance.util.Resource

class SiteRequestsActivity : ComponentActivity() {
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
                SiteRequestsScreen(site = site, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteRequestsScreen(
    site: SiteAssignment,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { SiteRequestRepository(AppDatabase.getInstance(context).siteRequestDao()) }
    val vm: SiteRequestsViewModel = viewModel(factory = SiteRequestsViewModelFactory(repo))

    LaunchedEffect(site.siteId) {
        vm.setSite(site.siteId)
    }

    val requests by vm.requests.observeAsState(emptyList())
    val saveResult by vm.saveResult.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveResult) {
        when (val r = saveResult) {
            is Resource.Success<*> -> snackbarHostState.showSnackbar("Saved")
            is Resource.Error -> snackbarHostState.showSnackbar(r.message)
            else -> Unit
        }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    fun submit() {
        val t = title.trim()
        val d = description.trim()
        if (t.isBlank() || d.isBlank()) {
            vm.showError("Title and description are required")
            return
        }
        val q = quantity.trim().toIntOrNull()
        vm.createRequest(t, d, q)
        title = ""
        description = ""
        quantity = ""
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Requests") },
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
            Text(text = site.siteName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Create a request locally per site. Remote submission will be added later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(
                onClick = { submit() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Request")
            }

            HorizontalDivider()

            if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No requests yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requests, key = { it.id }) { item ->
                        RequestRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestRow(item: SiteRequestEntity) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            val meta = buildString {
                append("Status: ")
                append(item.status)
                if (item.quantity != null) {
                    append("  |  Qty: ")
                    append(item.quantity)
                }
            }
            Text(text = meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
