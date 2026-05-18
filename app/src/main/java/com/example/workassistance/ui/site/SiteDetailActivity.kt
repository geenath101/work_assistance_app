package com.example.workassistance.ui.site

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workassistance.data.local.AppDatabase
import com.example.workassistance.data.remote.api.RetrofitClient
import com.example.workassistance.data.remote.model.Site
import com.example.workassistance.repository.AttendanceRepository
import com.example.workassistance.ui.theme.WorkAssistanceTheme
import com.example.workassistance.util.GeofenceHelper
import com.example.workassistance.util.Resource
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class SiteDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SITE_ID = "extra_site_id"
        const val EXTRA_SITE_NAME = "extra_site_name"
        const val EXTRA_SITE_ADDRESS = "extra_site_address"
        const val EXTRA_SITE_LAT = "extra_site_lat"
        const val EXTRA_SITE_LNG = "extra_site_lng"
        const val EXTRA_SITE_RADIUS = "extra_site_radius"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val site = Site(
            id = intent.getStringExtra(EXTRA_SITE_ID) ?: "",
            name = intent.getStringExtra(EXTRA_SITE_NAME) ?: "",
            address = intent.getStringExtra(EXTRA_SITE_ADDRESS) ?: "",
            latitude = intent.getDoubleExtra(EXTRA_SITE_LAT, 0.0),
            longitude = intent.getDoubleExtra(EXTRA_SITE_LNG, 0.0),
            radiusMeters = intent.getDoubleExtra(EXTRA_SITE_RADIUS, 100.0)
        )

        setContent {
            WorkAssistanceTheme {
                SiteDetailScreen(
                    site = site,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    site: Site,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).attendanceDao() }
    val repo = remember { AttendanceRepository(dao, RetrofitClient.apiService) }
    val siteViewModel: SiteViewModel = viewModel(factory = SiteViewModelFactory(repo))

    LaunchedEffect(site.id) {
        siteViewModel.loadSignInState(site.id)
    }

    val isSignedIn by siteViewModel.isSignedIn.observeAsState(false)
    val eventResult by siteViewModel.eventResult.observeAsState()

    // Location state
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    var locationStatus by remember { mutableStateOf("Waiting for location...") }
    var distanceText by remember { mutableStateOf("") }

    // Map marker ref — survives recomposition
    val userMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val mapViewRef = remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for event results
    LaunchedEffect(eventResult) {
        when (val r = eventResult) {
            is Resource.Success<*> -> snackbarHostState.showSnackbar("Recorded successfully")
            is Resource.Error -> snackbarHostState.showSnackbar(r.message)
            else -> Unit
        }
    }

    // Location updates
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                userLat = loc.latitude
                userLng = loc.longitude
                val distance = GeofenceHelper.distanceTo(loc.latitude, loc.longitude, site)
                val inside = distance <= site.radiusMeters
                locationStatus = if (inside) "You are inside the site area" else "You are outside the site area"
                distanceText = "Distance: ${distance.toInt()} m (allowed: ${site.radiusMeters.toInt()} m)"

                // Update map marker
                val gMap = mapViewRef.value ?: return
                val position = LatLng(loc.latitude, loc.longitude)
                val hue = if (inside) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                val marker = userMarkerRef.value
                if (marker == null) {
                    userMarkerRef.value = gMap.addMarker(
                        MarkerOptions().position(position).title("Your location")
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )
                } else {
                    marker.position = position
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(hue))
                }
            }
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    DisposableEffect(Unit) {
        if (hasLocationPermission) {
            @SuppressLint("MissingPermission")
            fun startUpdates() {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
                    .setMinUpdateIntervalMillis(3_000L)
                    .build()
                fusedClient.requestLocationUpdates(request, locationCallback, context.mainLooper)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLat = loc.latitude
                        userLng = loc.longitude
                    }
                }
            }
            startUpdates()
        } else {
            locationStatus = "Location permission not granted"
        }
        onDispose {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    val isActionLoading = eventResult is Resource.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(site.name) },
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
        ) {
            // Map — takes up most of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onCreate(null)
                            onResume()
                            getMapAsync { gMap ->
                                mapViewRef.value = gMap
                                gMap.uiSettings.isZoomControlsEnabled = true
                                val siteLatLng = LatLng(site.latitude, site.longitude)

                                // Site pin
                                gMap.addMarker(
                                    MarkerOptions()
                                        .position(siteLatLng)
                                        .title(site.name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )

                                // Geofence circle
                                gMap.addCircle(
                                    CircleOptions()
                                        .center(siteLatLng)
                                        .radius(site.radiusMeters)
                                        .strokeColor(0xFF1565C0.toInt())
                                        .strokeWidth(3f)
                                        .fillColor(0x221565C0)
                                )

                                // Zoom to site
                                val zoom = (16 - Math.log(site.radiusMeters / 100.0) / Math.log(2.0))
                                    .toFloat().coerceIn(10f, 20f)
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(siteLatLng, zoom))

                                // Place user marker if we already have location
                                val lat = userLat
                                val lng = userLng
                                if (lat != null && lng != null) {
                                    val inside = GeofenceHelper.isInsideGeofence(lat, lng, site)
                                    val hue = if (inside) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                                    userMarkerRef.value = gMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(lat, lng))
                                            .title("Your location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom sheet info panel
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = site.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = site.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Status chip
                    val chipColor = if (isSignedIn)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                    val chipTextColor = if (isSignedIn)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = chipColor
                    ) {
                        Text(
                            text = if (isSignedIn) "Signed In" else "Not Signed In",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = chipTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = locationStatus, style = MaterialTheme.typography.bodySmall)
                    if (distanceText.isNotEmpty()) {
                        Text(
                            text = distanceText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isActionLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sign In button
                        Button(
                            onClick = {
                                val lat = userLat ?: return@Button
                                val lng = userLng ?: return@Button
                                if (!GeofenceHelper.isInsideGeofence(lat, lng, site)) {
                                    val dist = GeofenceHelper.distanceTo(lat, lng, site)
                                    // Show via snackbar — handled in LaunchedEffect via a side channel
                                    return@Button
                                }
                                siteViewModel.signIn(site, lat, lng)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = userLat != null && !isSignedIn && !isActionLoading
                        ) {
                            Text("Sign In")
                        }

                        // Sign Out button
                        OutlinedButton(
                            onClick = {
                                val lat = userLat ?: return@OutlinedButton
                                val lng = userLng ?: return@OutlinedButton
                                siteViewModel.signOut(site, lat, lng)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = userLat != null && isSignedIn && !isActionLoading
                        ) {
                            Text("Sign Out")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
