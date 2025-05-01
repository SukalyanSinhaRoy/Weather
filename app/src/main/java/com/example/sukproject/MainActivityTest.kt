import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import com.example.sukproject.MainActivity

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testWeatherAppInteraction() {
        composeTestRule.setContent {
            WeatherApp(weatherViewModel = weatherViewModel, locationViewModel = locationViewModel, apiKey = "apiKey") { }
        }

        // Check if the UI displays the search bar
        composeTestRule.onNodeWithText("Search").assertExists()

        // Test entering a zip code and clicking search
        composeTestRule.onNodeWithText("Search").performClick()

        // Test if the weather data is displayed after search
        composeTestRule.onNodeWithText("Weather Data").assertExists()  // Adjust the text to match what is expected after fetching weather
    }

    @Test
    fun testLocationButtonClick() {
        composeTestRule.setContent {
            WeatherApp(weatherViewModel = weatherViewModel, locationViewModel = locationViewModel, apiKey = "apiKey") { }
        }

        // Test clicking on "Use My Location" button
        composeTestRule.onNodeWithText("Use My Location").performClick()

        // Test if the location service is triggered
        composeTestRule.onNodeWithText("Location Service").assertExists() // Adjust based on your actual logic
    }
}
