package com.example.sukproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppUI()
        }
    }
}

@Composable
fun WeatherAppUI() {
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

        Text(
            text = stringResource(R.string.location),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Row for temperature and icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.temperature), fontSize = 48.sp)
                Text(text = stringResource(R.string.feels_like), fontSize = 16.sp)
            }

            Image(
                painter = painterResource(id = R.drawable.weather_icon), // Your PNG file
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
            Text(text = stringResource(R.string.low_temp), fontSize = 16.sp)
            Text(text = stringResource(R.string.high_temp), fontSize = 16.sp)
            Text(text = stringResource(R.string.humidity), fontSize = 16.sp)
            Text(text = stringResource(R.string.pressure), fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherAppUI() {
    WeatherAppUI()
}
