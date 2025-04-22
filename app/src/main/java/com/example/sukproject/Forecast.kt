package com.example.sukproject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Keep the existing WeatherResponse class for current weather
// Don't redefine it here

// Renamed from ForecastResponse to avoid conflict
@Serializable
data class ForecastListResponse(
    val city: City,
    val list: List<ForecastItem>
)

@Serializable
data class City(
    val name: String
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: ForecastMain,
    val weather: List<Weather>,
    @SerialName("dt_txt")
    val dtTxt: String
)

@Serializable
data class ForecastMain(
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