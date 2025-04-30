package com.example.sukproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationService : Service() {
    private val TAG = "LocationService"
    private val CHANNEL_ID = "location_service_channel"
    private val NOTIFICATION_ID = 101

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var serviceScope = CoroutineScope(Dispatchers.IO)
    private var apiKey = "d3b9c9b3f931f10529f11fe91783d7e9" // Should be stored more securely in production

    // Listeners for location updates
    private val locationListeners = mutableListOf<(Location) -> Unit>()

    // Listeners for weather updates
    private val weatherListeners = mutableListOf<(WeatherResponse) -> Unit>()

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationService created")

        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "New location received: ${location.latitude}, ${location.longitude}")
                    currentLocation = location

                    // Notify all location listeners
                    locationListeners.forEach { it(location) }

                    // Fetch weather for this location and update notification
                    fetchWeatherForCurrentLocation()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with startId: $startId")

        val notification = createNotification("Getting your location...")

        // Create a pending intent to open the app when notification is tapped
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        }

        // Build updated notification with the intent
        val updatedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather App")
            .setContentText("Getting your location...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, updatedNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, updatedNotification)
        }

        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        Log.d(TAG, "LocationService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for location service notifications"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        // Create a pending intent to open the app when notification is tapped
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather App")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(weatherInfo: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(weatherInfo)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun startLocationUpdates() {
        try {
            // Request more frequent updates for better user experience
            val locationRequest = LocationRequest.create().apply {
                interval = 10 * 60 * 1000  // 10 minutes
                fastestInterval = 2 * 60 * 1000  // 2 minutes
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // Use HIGH_ACCURACY for better results
                maxWaitTime = 15 * 60 * 1000  // 15 minutes max wait time
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            Log.d(TAG, "Started location updates with priority: HIGH_ACCURACY")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Stopped location updates")
    }

    fun getLastLocation(callback: (Location?) -> Unit) {
        if (currentLocation != null) {
            Log.d(TAG, "Returning cached location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
            callback(currentLocation)
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "Got last location: ${location.latitude}, ${location.longitude}")
                        currentLocation = location
                        callback(location)

                        // Fetch weather for this location immediately
                        fetchWeatherForCurrentLocation()
                    } else {
                        Log.d(TAG, "Last location is null, waiting for location updates")
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get last location", e)
                    callback(null)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            callback(null)
        }
    }

    private fun fetchWeatherForCurrentLocation() {
        currentLocation?.let { location ->
            serviceScope.launch {
                try {
                    Log.d(TAG, "Fetching weather for location: ${location.latitude}, ${location.longitude}")
                    val lat = location.latitude
                    val lon = location.longitude

                    val weatherResponse = RetrofitInstance.api.getCurrentWeatherByCoordinates(
                        lat.toString(),
                        lon.toString(),
                        apiKey
                    )

                    Log.d(TAG, "Weather fetched for: ${weatherResponse.name}")

                    // Update notification with current weather
                    val weatherInfo = "${weatherResponse.name}: ${weatherResponse.main.temp}°, " +
                            (weatherResponse.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "")
                    updateNotification(weatherInfo)

                    // Notify all weather listeners
                    weatherListeners.forEach { it(weatherResponse) }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch weather", e)
                }
            }
        } ?: Log.d(TAG, "Cannot fetch weather: location is null")
    }

    fun addLocationListener(listener: (Location) -> Unit) {
        locationListeners.add(listener)
        // Immediately provide last location if available
        currentLocation?.let { listener(it) }
    }

    fun removeLocationListener(listener: (Location) -> Unit) {
        locationListeners.remove(listener)
    }

    fun addWeatherListener(listener: (WeatherResponse) -> Unit) {
        weatherListeners.add(listener)
    }

    fun removeWeatherListener(listener: (WeatherResponse) -> Unit) {
        weatherListeners.remove(listener)
    }
}