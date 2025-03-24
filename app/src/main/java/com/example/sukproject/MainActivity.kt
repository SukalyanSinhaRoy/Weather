package com.example.sukproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val apiKey = "d3b9c9b3f931f10529f11fe91783d7e9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch weather data when activity is created
        weatherViewModel.fetchWeather("St. Paul", apiKey)

        setContent {
            WeatherAppUI()
        }
    }
}

@Composable
fun WeatherAppUI(weatherViewModel: WeatherViewModel) {
    val weatherData by weatherViewModel.weatherData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        weatherData?.let { data ->
            Text(text = data.name, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "${data.main.temp}°", fontSize = 48.sp)
                    Text(text = "Feels like ${data.main.feels_like}°", fontSize = 16.sp)
                }

                Image(
                    painter = painterResource(id = R.drawable.weather_icon),
                    contentDescription = stringResource(R.string.weather_icon_desc),
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Low ${data.main.temp_min}°", fontSize = 16.sp)
                Text(text = "High ${data.main.temp_max}°", fontSize = 16.sp)
                Text(text = "Humidity ${data.main.humidity}%", fontSize = 16.sp)
                Text(text = "Pressure ${data.main.pressure} hPa", fontSize = 16.sp)
            }
        } ?: run {
            Text(text = "Loading...", fontSize = 18.sp, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherAppUI() {
    val previewViewModel = WeatherViewModel()
    WeatherAppUI(weatherViewModel = previewViewModel)
}
