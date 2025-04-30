package com.example.sukproject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _sixteenDayForecastData = MutableLiveData<SixTeenDayForecast?>()
    val sixteenDayForecastData: LiveData<SixTeenDayForecast?> = _sixteenDayForecastData

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentZipCode: String = ""
    private var currentLocation: String? = null
    var currentLatitude: Double? = null
    var currentLongitude: Double? = null

    fun fetchWeatherByZip(zip: String, apiKey: String) {
        if (!isValidZipCode(zip)) {
            _errorState.postValue("Please enter a valid 5-digit zip code")
            return
        }

        currentZipCode = zip
        currentLocation = "$zip,us"
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCurrentWeatherByZip("$zip,us", apiKey)
                }
                _weatherData.postValue(response)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Could not find weather for zip code $zip")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeatherByCoordinates(latitude: Double, longitude: Double, apiKey: String) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentLocation = null // Reset current location name since we're using coordinates
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCurrentWeatherByCoordinates(
                        latitude.toString(),
                        longitude.toString(),
                        apiKey
                    )
                }
                // Update the current location with the city name from the response
                currentLocation = response.name
                _weatherData.postValue(response)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Could not fetch weather for this location: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSixteenDayForecast(apiKey: String) {
        if (currentLocation.isNullOrEmpty()) {
            _errorState.postValue("Please search for a location first")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getForecastSixteenDays(currentLocation!!, 15, apiKey)
                }
                _sixteenDayForecastData.postValue(response)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Could not load 16-day forecast: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSixteenDayForecastByCoordinates(apiKey: String) {
        if (currentLatitude == null || currentLongitude == null) {
            _errorState.postValue("Location coordinates not available")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // First get the current weather to ensure we have a city name
                val weatherResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCurrentWeatherByCoordinates(
                        currentLatitude.toString(),
                        currentLongitude.toString(),
                        apiKey
                    )
                }

                // Update the current location with the city name from the response
                currentLocation = weatherResponse.name

                // Now fetch the forecast using the city name
                val forecastResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getForecastSixteenDays(weatherResponse.name, 15, apiKey)
                }

                _sixteenDayForecastData.postValue(forecastResponse)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Could not load 16-day forecast: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateWeatherFromResponse(weatherResponse: WeatherResponse) {
        // Called when location service provides a weather update
        _weatherData.value = weatherResponse
        currentLocation = weatherResponse.name
    }

    private fun isValidZipCode(zip: String): Boolean {
        return zip.length == 5 && zip.all { it.isDigit() }
    }

    fun clearError() {
        _errorState.value = null
    }
}