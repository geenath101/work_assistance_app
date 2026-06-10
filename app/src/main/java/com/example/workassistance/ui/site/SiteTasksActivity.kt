package com.example.workassistance.ui.site

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workassistance.data.local.AppDatabase
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.data.remote.api.RetrofitClient
import com.example.workassistance.data.local.entity.SiteTaskEntity
import com.example.workassistance.repository.SiteTaskRepository
import com.example.workassistance.ui.theme.WorkAssistanceTheme
import com.example.workassistance.util.Resource

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
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { SiteTaskRepository(db.siteTaskDao(), db.proofOfWorkDao(), RetrofitClient.apiService) }
    val vm: SiteTasksViewModel = viewModel(factory = SiteTasksViewModelFactory(repo))

    LaunchedEffect(site.siteId) {
        vm.setSite(site.siteId)
        vm.refresh()
    }

    val tasks by vm.tasks.observeAsState(emptyList())
    val loadResult by vm.loadResult.observeAsState()
    val actionResult by vm.actionResult.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(loadResult) {
        when (val r = loadResult) {
            is Resource.Error -> snackbarHostState.showSnackbar(r.message)
            else -> Unit
        }
    }
    LaunchedEffect(actionResult) {
        when (val r = actionResult) {
            is Resource.Success<*> -> snackbarHostState.showSnackbar("Task completed")
            is Resource.Error -> snackbarHostState.showSnackbar(r.message)
            else -> Unit
        }
    }

    val isLoading = loadResult is Resource.Loading || actionResult is Resource.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Assigned Tasks") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    ,
                    actions = {
                        IconButton(onClick = { vm.refresh() }, enabled = !isLoading) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            HorizontalDivider()

            if (tasks.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks for this site",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.taskId }) { task ->
                        TaskCard(
                            task = task,
                            onAttachProof = {
                                context.startActivity(Intent(context, ProofOfWorkActivity::class.java).apply {
                                    putExtra(SiteDetailActivity.EXTRA_ASSIGNMENT_ID, site.assignmentId)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_ID, site.siteId)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_NAME, site.siteName)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_ADDRESS, site.siteAddress)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_LAT, site.latitude)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_LNG, site.longitude)
                                    putExtra(SiteDetailActivity.EXTRA_SITE_RADIUS, site.radiusMeters)
                                    putExtra(SiteDetailActivity.EXTRA_SIGN_IN_EXPIRY_MINUTES, site.signInExpiryMinutes)
                                    putExtra(ProofOfWorkActivity.EXTRA_TASK_ID, task.taskId)
                                    putExtra(ProofOfWorkActivity.EXTRA_TASK_TITLE, task.title)
                                })
                            },
                            onComplete = {
                                if (task.isCompleted) {
                                    vm.showError("Task already completed")
                                } else {
                                    vm.completeTask(task.taskId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: SiteTaskEntity,
    onAttachProof: () -> Unit,
    onComplete: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAttachProof,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Attach proof")
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    enabled = !task.isCompleted
                ) {
                    Text(if (task.isCompleted) "Completed" else "Complete")
                }
            }
        }
    }
}
