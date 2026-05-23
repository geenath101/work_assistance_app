package com.example.workassistance.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workassistance.data.remote.api.RetrofitClient
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.AuthRepository
import com.example.workassistance.repository.SiteRepository
import com.example.workassistance.ui.main.MainViewModel
import com.example.workassistance.ui.main.MainViewModelFactory
import com.example.workassistance.util.Resource

@Composable
fun HomeScreen(
    onSiteClicked: (SiteAssignment) -> Unit
) {
    val employeeId = remember {
        AuthRepository.getCurrentUser()?.employeeId ?: ""
    }

    val viewModel: MainViewModel = viewModel(
        key = employeeId,
        factory = MainViewModelFactory(SiteRepository(RetrofitClient.apiService), employeeId)
    )

    val sitesResource by viewModel.sites.observeAsState(Resource.Loading)

    Column(modifier = Modifier.fillMaxSize()) {
        when (val resource = sitesResource) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = resource.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadSites() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }

            is Resource.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val sites = (resource as Resource.Success<List<SiteAssignment>>).data
                if (sites.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No upcoming site visits", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sites) { site ->
                            SiteCard(site = site, onClick = { onSiteClicked(site) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SiteCard(site: SiteAssignment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.siteName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = site.siteAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Allowed radius: ${site.radiusMeters.toInt()} m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (site.shiftSlots.isNotEmpty()) {
                    val days = site.shiftSlots.joinToString(", ") { slot ->
                        "${slot.day} ${slot.shiftStart}–${slot.shiftEnd}"
                    }
                    Text(
                        text = days,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
