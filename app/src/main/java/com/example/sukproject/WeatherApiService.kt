package com.example.sukproject

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Define your API response model
@Serializable
data class WeatherResponse(
    val name: String, // City name
    val main: Main,
    val weather: List<Weather>
)

@Serializable
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class Weather(
    val description: String
)

// Retrofit Interface for API Calls
interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial" // Get Fahrenheit temperature
    ): WeatherResponse
}

// Singleton Retrofit Instance
object RetrofitInstance {
    private val json = Json { ignoreUnknownKeys = true }

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApiService::class.java)
    }
}
