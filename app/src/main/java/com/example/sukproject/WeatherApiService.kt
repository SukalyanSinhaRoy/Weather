package com.example.sukproject

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApiService::class.java)
    }
}