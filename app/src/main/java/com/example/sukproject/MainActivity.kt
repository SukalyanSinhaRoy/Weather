package com.example.sukproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val apiKey = "d3b9c9b3f931f10529f11fe91783d7e9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherApp(weatherViewModel = weatherViewModel, apiKey = apiKey)
        }
    }
}

@Composable
fun WeatherApp(weatherViewModel: WeatherViewModel, apiKey: String) {
    val navController = rememberNavController()
    val errorState = weatherViewModel.errorState.observeAsState()

    // Show error dialog if there's an error
    if (!errorState.value.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { weatherViewModel.clearError() },
            title = { Text(text = stringResource(R.string.error_title)) },
            text = { Text(text = errorState.value!!) },
            confirmButton = {
                Button(onClick = { weatherViewModel.clearError() }) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }

    NavHost(navController = navController, startDestination = "currentWeather") {
        composable("currentWeather") {
            CurrentWeatherScreen(
                weatherViewModel = weatherViewModel,
                apiKey = apiKey,
                navController = navController
            )
        }
        composable("forecast") {
            ForecastScreen(
                weatherViewModel = weatherViewModel,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentWeatherScreen(
    weatherViewModel: WeatherViewModel,
    apiKey: String,
    navController: NavHostController
) {
    val weatherData = weatherViewModel.weatherData.observeAsState()
    val isLoading = weatherViewModel.isLoading.observeAsState(initial = false)
    val context = LocalContext.current
    var zipCode by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Zip code input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = zipCode,
                onValueChange = {
                    if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                        zipCode = it
                    }
                },
                label = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { weatherViewModel.fetchWeatherByZip(zipCode, apiKey) },
                enabled = zipCode.length == 5 && !isLoading.value
            ) {
                Text(stringResource(R.string.search_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading.value) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.loading),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            weatherData.value?.let { data ->
                Text(text = data.name, fontSize = 24.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${data.main.temp.toInt()}°", fontSize = 64.sp)
                        Text(
                            text = stringResource(R.string.feels_like_template, data.main.feelsLike),
                            fontSize = 18.sp
                        )
                    }


                    Image(
                        painter = painterResource(id = R.drawable.weather_icon),
                        contentDescription = stringResource(R.string.weather_icon_desc),
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.low_temp_template, data.main.tempMin),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.high_temp_template, data.main.tempMax),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.humidity_template, data.main.humidity),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.pressure_template, data.main.pressure),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        weatherViewModel.fetchForecast(apiKey)
                        navController.navigate("forecast")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(R.string.view_forecast))
                }
            } ?: run {
                Text(
                    text = stringResource(R.string.no_weather_data),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    weatherViewModel: WeatherViewModel,
    navController: NavHostController
) {
    val forecastData = weatherViewModel.dailyForecastData.observeAsState()
    val isLoading = weatherViewModel.isLoading.observeAsState(initial = false)

    // Create a date formatter that shows only the day name and date
    val dateFormat = stringResource(R.string.forecast_date_format)
    val dateFormatter = SimpleDateFormat(dateFormat, Locale.US)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.title_forecast)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_current)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        if (isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.loading))
                }
            }
        } else {
            forecastData.value?.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(data.list) { forecastItem ->
                        ForecastItemCard(forecastItem, dateFormatter)
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_weather_data),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastItemCard(forecastItem: ForecastItem, dateFormatter: SimpleDateFormat) {
    val date = try {
        // Parse dtTxt to get the date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        inputFormat.parse(forecastItem.dtTxt)
    } catch (e: Exception) {
        // Fallback to using Unix timestamp if dtTxt parsing fails
        Date(forecastItem.dt * 1000L)
    }

    val formattedDate = try {
        dateFormatter.format(date ?: Date())
    } catch (e: Exception) {
        "Invalid date"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (forecastItem.weather.isNotEmpty()) {
                    Text(
                        text = forecastItem.weather[0].description.replaceFirstChar { it.uppercase() },
                        fontSize = 16.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${forecastItem.main.temp.toInt()}°",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "H: ${forecastItem.main.tempMax.toInt()}° L: ${forecastItem.main.tempMin.toInt()}°",
                    fontSize = 16.sp
                )
            }
        }
    }
}


// Extension function to capitalize the first letter of a string
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}