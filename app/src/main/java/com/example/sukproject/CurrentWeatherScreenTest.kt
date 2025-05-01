import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import com.example.sukproject.CurrentWeatherScreen

class CurrentWeatherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWeatherScreenDisplaysData() {
        composeTestRule.setContent {
            CurrentWeatherScreen(
                weatherViewModel = weatherViewModel,
                locationViewModel = locationViewModel,
                apiKey = "apiKey",
                navController = navController,
                onLocationPermissionRequest = {}
            )
        }

        // Test if weather data is displayed
        composeTestRule.onNodeWithText("25°").assertExists()  // Replace with actual weather data text
        composeTestRule.onNodeWithText("Humidity: 50%").assertExists()  // Replace with actual humidity value
    }

    @Test
    fun testZipCodeSearch() {
        composeTestRule.setContent {
            CurrentWeatherScreen(
                weatherViewModel = weatherViewModel,
                locationViewModel = locationViewModel,
                apiKey = "apiKey",
                navController = navController,
                onLocationPermissionRequest = {}
            )
        }

        // Test entering a zip code and submitting the search
        composeTestRule.onNodeWithText("Enter Zip Code").performTextInput("12345")
        composeTestRule.onNodeWithText("Search").performClick()

        // Check if the weather data displays for the zip code
        composeTestRule.onNodeWithText("Weather Data").assertExists()  // Adjust as needed
    }
}
