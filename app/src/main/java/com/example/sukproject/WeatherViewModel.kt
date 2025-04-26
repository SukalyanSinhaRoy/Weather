package com.example.sukproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _forecastData = MutableLiveData<ForecastListResponse?>()
    val forecastData: LiveData<ForecastListResponse?> = _forecastData

    // New LiveData for daily forecast
    private val _dailyForecastData = MutableLiveData<ForecastListResponse?>()
    val dailyForecastData: LiveData<ForecastListResponse?> = _dailyForecastData

    private val _sixteenDayForecastData = MutableLiveData<ForecastListResponse?>()
    val sixteenDayForecastData: LiveData<ForecastListResponse?> = _sixteenDayForecastData

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState


    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentZipCode: String = ""

    fun fetchWeatherByCity(city: String, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCurrentWeather(city, apiKey)
                }
                _weatherData.postValue(response)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue(e.message ?: "Unknown error occurred")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeatherByZip(zip: String, apiKey: String) {
        if (!isValidZipCode(zip)) {
            _errorState.postValue("Please enter a valid 5-digit zip code")
            return
        }

        currentZipCode = zip
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

    fun fetchSixteenDayForecast(apiKey: String) {
        if (currentZipCode.isEmpty()) {
            _errorState.postValue("Please search for a location first")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getForecastSixteenDays("$currentZipCode,us", 16, apiKey)
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


    private fun isValidZipCode(zip: String): Boolean {
        return zip.length == 5 && zip.all { it.isDigit() }
    }

    fun fetchWeather(city: String, apiKey: String) {
        fetchWeatherByCity(city, apiKey)
    }

    fun clearError() {
        _errorState.value = null
    }
}