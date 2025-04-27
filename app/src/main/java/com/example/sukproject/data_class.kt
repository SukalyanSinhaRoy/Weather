package com.example.sukproject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like")
    val feelsLike: Double,
    @SerialName("temp_min")
    val tempMin: Double,
    @SerialName("temp_max")
    val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class Weather(
    val description: String,
    val icon: String
)