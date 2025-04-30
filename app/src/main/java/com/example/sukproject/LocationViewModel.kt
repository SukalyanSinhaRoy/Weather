package com.example.sukproject

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LocationViewModel"

    private val _isLocationEnabled = MutableLiveData(false)
    val isLocationEnabled: LiveData<Boolean> = _isLocationEnabled

    private val _locationBasedWeather = MutableLiveData<WeatherResponse?>(null)
    val locationBasedWeather: LiveData<WeatherResponse?> = _locationBasedWeather

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentLocation: Location? = null
    private var locationService: LocationService? = null
    private var bound = false

    // Make latitude and longitude accessible from outside
    var currentLatitude: Double? = null
        private set
    var currentLongitude: Double? = null
        private set

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            bound = true
            Log.d(TAG, "Service connected")

            // Now that we're connected, listen for location updates
            setupLocationListener()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            bound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    // Location listener to get location updates
    private val locationListener: (Location) -> Unit = { location ->
        Log.d(TAG, "Location update received: ${location.latitude}, ${location.longitude}")
        // Save current location
        currentLocation = location
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    private val weatherListener: (WeatherResponse) -> Unit = { weatherResponse ->
        Log.d(TAG, "Weather update received for location: ${weatherResponse.name}")
        _locationBasedWeather.postValue(weatherResponse)
        _isLoading.postValue(false)
    }

    fun bindLocationService() {
        if (bound) return // Don't bind again if already bound

        _isLoading.value = true
        val context = getApplication<Application>()
        val intent = Intent(context, LocationService::class.java)

        try {
            // Start and bind the service
            context.startService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            _isLocationEnabled.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind location service", e)
            _errorMessage.value = "Failed to start location service: ${e.message}"
            _isLoading.value = false
        }
    }

    fun unbindLocationService() {
        if (bound) {
            try {
                locationService?.let { service ->
                    service.removeLocationListener(locationListener)
                    service.removeWeatherListener(weatherListener)
                }

                getApplication<Application>().unbindService(serviceConnection)
                bound = false
                _isLocationEnabled.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unbind service", e)
            }
        }
    }

    private fun setupLocationListener() {
        locationService?.let { service ->
            // Add both location and weather listeners
            service.addLocationListener(locationListener)
            service.addWeatherListener(weatherListener)

            // Get last location if available
            service.getLastLocation { location ->
                if (location == null) {
                    _errorMessage.postValue("Waiting for location...")
                } else {
                    Log.d(TAG, "Last known location: ${location.latitude}, ${location.longitude}")
                    // Save current location
                    currentLocation = location
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            }
        }
    }

    fun getCurrentCoordinates(callback: (Double?, Double?) -> Unit) {
        if (currentLocation != null) {
            callback(currentLocation!!.latitude, currentLocation!!.longitude)
            return
        }

        // Try to get location from service
        locationService?.getLastLocation { location ->
            if (location != null) {
                currentLocation = location
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                callback(location.latitude, location.longitude)
            } else {
                callback(null, null)
            }
        } ?: callback(null, null)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        unbindLocationService()
    }
}