package com.example.sukproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
        private val weatherViewModel: WeatherViewModel by viewModels()
        private val locationViewModel: LocationViewModel by viewModels()
        private val apiKey = "d3b9c9b3f931f10529f11fe91783d7e9"

        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private val CHANNEL_ID = "weather_channel"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true

        if (locationGranted) {
            locationViewModel.bindLocationService()
        }
        if(notificationGranted){
            println("finally")
        }
    }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            createNotificationChannel()

            // Start location service at app launch
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationViewModel.bindLocationService()
            }

            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WeatherApp(
                            weatherViewModel = weatherViewModel,
                            locationViewModel = locationViewModel,
                            apiKey = apiKey,
                            onLocationPermissionRequest = { requestLocationPermissions() }
                        )
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun requestLocationPermissions() {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

       fun showPersistentNotification(temp: String, condition: String, locationName: String) {
           println("Invoked showPersistentNotification ${temp} ${condition} ${locationName}")
           val intent = Intent(this, MainActivity::class.java)
           val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("$temp • $condition")
                .setContentText(locationName)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true)

            val context = this
           println("before the with clause")

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    println("Permissions failed :(")
                    return
                }
                println("Permissions succeded :)")
                notify(1, builder.build())
            }
           println("Invoked notifications")
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Weather Channel"
                val descriptionText = "Shows current weather information"
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }



@Composable
fun WeatherApp(
    weatherViewModel: WeatherViewModel,
    locationViewModel: LocationViewModel,
    apiKey: String,
    onLocationPermissionRequest: () -> Unit
) {
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
                locationViewModel = locationViewModel,
                apiKey = apiKey,
                navController = navController,
                onLocationPermissionRequest = onLocationPermissionRequest
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
    locationViewModel: LocationViewModel,
    apiKey: String,
    navController: NavHostController,
    onLocationPermissionRequest: () -> Unit
) {
    val weatherData = weatherViewModel.weatherData.observeAsState()
    val locationWeather = locationViewModel.locationBasedWeather.observeAsState()
    val isLoading = weatherViewModel.isLoading.observeAsState(initial = false)
    val isLocationLoading = locationViewModel.isLoading.observeAsState(initial = false)
    val context = LocalContext.current
    var zipCode by rememberSaveable { mutableStateOf("") }

    // Track whether we're using location-based weather or zip-based weather
    val isUsingLocationWeather = locationWeather.value != null && zipCode.isBlank()

    // Use locationWeather if available, otherwise use weatherData
    val currentWeather = when {
        zipCode.isNotBlank() && weatherData.value != null -> weatherData.value
        locationWeather.value != null -> locationWeather.value
        else -> null
    }

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

        // Zip code input field and buttons
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

        // Use My Location button
        Button(
            onClick = { onLocationPermissionRequest() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use My Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading.value || isLocationLoading.value) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.loading),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            currentWeather?.let { data ->
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
                        // Check if we're using location-based weather or zip-based weather
                        if (isUsingLocationWeather) {
                            // Use coordinates for forecast
                            locationViewModel.getCurrentCoordinates { latitude, longitude ->
                                if (latitude != null && longitude != null) {
                                    weatherViewModel.currentLatitude = latitude
                                    weatherViewModel.currentLongitude = longitude
                                    weatherViewModel.fetchSixteenDayForecastByCoordinates(apiKey)
                                } else {
                                    weatherViewModel.fetchSixteenDayForecast(apiKey)
                                }
                                navController.navigate("forecast")
                            }
                        } else {
                            // Use the current method for zip-based weather
                            weatherViewModel.fetchSixteenDayForecast(apiKey)
                            navController.navigate("forecast")
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(R.string.view_forecast))
                }
                val activity = context as? MainActivity
                activity?.showPersistentNotification(
                    temp = "${data.main.temp.toInt()}°",
                    condition = data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                    locationName = data.name
                )
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
    val fullForecastData = weatherViewModel.sixteenDayForecastData.observeAsState()
    val isLoading = weatherViewModel.isLoading.observeAsState(initial = false)

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
            fullForecastData.value?.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(data.list) { forecastItem ->
                        ForecastItemCard(forecastItem)
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
fun ForecastItemCard(forecastItem: ForecastItemSchema) {
    val date = Date(forecastItem.dt * 1000L)
    val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

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
                    text = dateFormatter.format(date),
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
                    text = "${forecastItem.temp.max}°",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${forecastItem.temp.min}°",
                    fontSize = 16.sp
                )
            }
        }
    }
}