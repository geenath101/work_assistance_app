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
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
                    val now = ZonedDateTime.now()
                    val (startingSoon, later) = remember(sites, now) {
                        val withNext = sites.map { site ->
                            val next = nextShiftStart(site, now)
                            site to next
                        }

                        val soon = withNext
                            .filter { (_, next) ->
                                if (next == null) return@filter false
                                val hours = Duration.between(now.toInstant(), next.toInstant()).toHours()
                                hours in 0..7
                            }
                            .sortedBy { it.second }
                            .map { it.first }

                        val laterSites = withNext
                            .filterNot { (site, _) -> soon.any { it.siteId == site.siteId } }
                            .sortedBy { (_, next) -> next?.toInstant()?.toEpochMilli() ?: Long.MAX_VALUE }
                            .map { it.first }

                        soon to laterSites
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Starting Soon (next 8 hours)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (startingSoon.isEmpty()) "No sites starting in the next 8 hours" else "${startingSoon.size} site(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (startingSoon.isNotEmpty()) {
                            items(startingSoon, key = { it.siteId }) { site ->
                                SiteCard(site = site, onClick = { onSiteClicked(site) })
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upcoming Later",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (later.isEmpty()) "No other upcoming sites" else "${later.size} site(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (later.isNotEmpty()) {
                            items(later, key = { it.siteId }) { site ->
                                SiteCard(site = site, onClick = { onSiteClicked(site) })
                            }
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
                        "${slot.day} ${formatTimeWithAmPm(slot.shiftStart)}–${formatTimeWithAmPm(slot.shiftEnd)}"
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

private fun nextShiftStart(site: SiteAssignment, now: ZonedDateTime): ZonedDateTime? {
    if (site.shiftSlots.isEmpty()) return null

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val nowTime = now.toLocalTime()
    val todayDow = today.dayOfWeek

    var best: ZonedDateTime? = null
    for (slot in site.shiftSlots) {
        val dow = parseDayOfWeek(slot.day) ?: continue
        val startTime = parseTime(slot.shiftStart) ?: continue

        var daysUntil = (dow.value - todayDow.value + 7) % 7
        if (daysUntil == 0 && !startTime.isAfter(nowTime)) {
            // Shift for today already started (or equals now) -> next week's occurrence.
            daysUntil = 7
        }
        val date = today.plusDays(daysUntil.toLong())
        val candidate = date.atTime(startTime).atZone(zone)
        if (candidate.isAfter(now)) {
            if (best == null || candidate.isBefore(best)) best = candidate
        }
    }
    return best
}

private fun parseDayOfWeek(raw: String): DayOfWeek? {
    val s = raw.trim().lowercase()
    return when {
        s.startsWith("mon") -> DayOfWeek.MONDAY
        s.startsWith("tue") -> DayOfWeek.TUESDAY
        s.startsWith("wed") -> DayOfWeek.WEDNESDAY
        s.startsWith("thu") -> DayOfWeek.THURSDAY
        s.startsWith("fri") -> DayOfWeek.FRIDAY
        s.startsWith("sat") -> DayOfWeek.SATURDAY
        s.startsWith("sun") -> DayOfWeek.SUNDAY
        else -> null
    }
}

private fun parseTime(raw: String): LocalTime? {
    val s = raw.trim().replace(".", ":")
    val upper = s.uppercase()

    // If API sends AM/PM, prefer that.
    if (upper.contains("AM") || upper.contains("PM")) {
        val normalized = upper.replace(Regex("\\s+"), " ")
        val patterns = listOf("h:mm a", "h a", "hh:mm a", "hh a")
        for (p in patterns) {
            try {
                return LocalTime.parse(normalized, DateTimeFormatter.ofPattern(p))
            } catch (_: DateTimeParseException) {
            }
        }
    }

    // Otherwise attempt 24h formats (common for APIs).
    val patterns24 = listOf("H:mm", "HH:mm", "H", "HH")
    for (p in patterns24) {
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern(p))
        } catch (_: DateTimeParseException) {
        }
    }

    // Last attempt: if it's like "9:00" treat as 9 AM.
    return try {
        LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"))
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun formatTimeWithAmPm(raw: String): String {
    val t = parseTime(raw) ?: return raw
    return t.format(DateTimeFormatter.ofPattern("h:mm a"))
}
