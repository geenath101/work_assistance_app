package com.example.workassistance.util

import android.location.Location
import com.example.workassistance.data.remote.model.SiteAssignment

object GeofenceHelper {

    /**
     * Returns true if [userLat]/[userLng] is within the site's configured
     * sign-in radius. Uses the coordinates and radius embedded in the
     * [SiteAssignment] from the initial employee-sites API response —
     * no secondary network call needed.
     */
    fun isInsideGeofence(userLat: Double, userLng: Double, site: SiteAssignment): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, site.latitude, site.longitude, results)
        return results[0] <= site.radiusMeters
    }

    /**
     * Returns the distance in metres between the user and the site centre.
     */
    fun distanceTo(userLat: Double, userLng: Double, site: SiteAssignment): Float {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, site.latitude, site.longitude, results)
        return results[0]
    }
}
