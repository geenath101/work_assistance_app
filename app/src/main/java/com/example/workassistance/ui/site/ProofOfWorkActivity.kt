package com.example.workassistance.ui.site

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
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
import com.example.workassistance.data.local.entity.ProofOfWorkPhoto
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.ProofOfWorkRepository
import com.example.workassistance.ui.theme.WorkAssistanceTheme
import com.example.workassistance.util.Resource

class ProofOfWorkActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

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

        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE)

        setContent {
            WorkAssistanceTheme {
                ProofOfWorkScreen(
                    site = site,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProofOfWorkScreen(
    site: SiteAssignment,
    taskId: String?,
    taskTitle: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ProofOfWorkRepository(AppDatabase.getInstance(context).proofOfWorkDao()) }
    val vm: ProofOfWorkViewModel = viewModel(factory = ProofOfWorkViewModelFactory(repo))

    LaunchedEffect(site.siteId, taskId) {
        vm.setScope(site.siteId, taskId)
    }

    val photos by vm.photos.observeAsState(emptyList())
    val saveResult by vm.saveResult.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveResult) {
        when (val r = saveResult) {
            is Resource.Success<*> -> snackbarHostState.showSnackbar("Saved")
            is Resource.Error -> snackbarHostState.showSnackbar(r.message)
            else -> Unit
        }
    }

    var note by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Persist read permission when possible (for ACTION_OPEN_DOCUMENT style Uris)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Not persistable for all Uris; still store and try to read later.
            }
            vm.addPhoto(uri.toString(), note.ifBlank { null })
            note = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (taskTitle.isNullOrBlank()) "Proof of Work" else "Proof: $taskTitle") },
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
                text = if (taskId.isNullOrBlank()) {
                    "Store work photos locally for this site."
                } else {
                    "Attach at least 1 photo to complete this task."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Button(
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Photo")
            }

            HorizontalDivider()

            if (photos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No photos yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos, key = { it.id }) { item ->
                        ProofPhotoRow(item = item, onDelete = { vm.delete(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProofPhotoRow(
    item: ProofOfWorkPhoto,
    onDelete: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.uri, style = MaterialTheme.typography.bodySmall)
                if (!item.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.note!!, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
