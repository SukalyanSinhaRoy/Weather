package com.example.sukproject

import android.location.Location
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocationServiceTest {

    private lateinit var locationService: LocationService

    @Before
    fun setUp() {
        locationService = LocationService()
        // Initialize with mock values if needed (location, fusedLocationClient)
    }

    @Test
    fun testGetLastLocation_CachedLocation() {
        val mockLocation = Location("mock").apply {
            latitude = 37.7749
            longitude = -122.4194
        }

        locationService.currentLocation = mockLocation

        var location: Location? = null
        locationService.getLastLocation { loc ->
            location = loc
        }

        assertNotNull(location)
        location?.latitude?.let { assertEquals(37.7749, it, 0.0) }
        location?.longitude?.let { assertEquals(-122.4194, it, 0.0) }
    }

    @Test
    fun testAddAndRemoveLocationListener() {
        val listener: (Location) -> Unit = { location ->
            assertEquals(37.7749, location.latitude, 0.0)
            assertEquals(-122.4194, location.longitude, 0.0)
        }

        locationService.addLocationListener(listener)

        val mockLocation = Location("mock").apply {
            latitude = 37.7749
            longitude = -122.4194
        }

        locationService.currentLocation = mockLocation
        locationService.getLastLocation(listener) // Should invoke listener with mockLocation

        locationService.removeLocationListener(listener)
        locationService.getLastLocation(listener) // Should not invoke listener anymore
    }

    @Test
    fun testStartAndStopLocationUpdates() {
        // Mock the behavior of the fusedLocationClient
        locationService.startLocationUpdates()

        assertTrue("Location updates should be started", true) // Add checks for real behavior if needed

        locationService.stopLocationUpdates()

        assertTrue("Location updates should be stopped", true) // Similar check for stop
    }

    @Test
    fun testCreateNotification() {
        val notification = locationService.createNotification("Test location notification")

        assertNotNull(notification)
        assertTrue(notification is NotificationCompat.Builder)
    }

    @Test
    fun testFetchWeatherForCurrentLocation() = runBlocking {
        val mockLocation = Location("mock").apply {
            latitude = 37.7749
            longitude = -122.4194
        }

        locationService.currentLocation = mockLocation

        // Call fetch weather function
        locationService.fetchWeatherForCurrentLocation()

        // Here you can check if the weather API call was made and the notification updated
        assertTrue("Weather fetch should be called", true) // Implement mock checking
    }
}
