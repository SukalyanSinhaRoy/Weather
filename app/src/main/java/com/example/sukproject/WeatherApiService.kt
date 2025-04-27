package com.example.sukproject

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit Interface for API Calls
interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse

    @GET("weather")
    suspend fun getCurrentWeatherByZip(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast/daily")
    suspend fun getForecastSixteenDays(
        @Query("q") location: String,
        @Query("cnt") count: Int = 16,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): SixTeenDayForecast

}

// Singleton Retrofit Instance
object RetrofitInstance {
    private val json = Json { ignoreUnknownKeys = true }

    val api: WeatherApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Or BASIC for just the URL and headers
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(client) // Set the OkHttpClient with the interceptor
            .build()
            .create(WeatherApiService::class.java)
    }
}