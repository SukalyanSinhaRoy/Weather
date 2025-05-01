import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class WeatherViewModelTest {

    private lateinit var weatherViewModel: WeatherViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var errorStateObserver: Observer<String>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        weatherViewModel = WeatherViewModel() // Initialize your ViewModel
        weatherViewModel.errorState.observeForever(errorStateObserver)
    }

    @Test
    fun testFetchWeatherByZipCode_success() {
        // Simulate a successful weather data fetch
        val zipCode = "12345"
        weatherViewModel.fetchWeatherByZip(zipCode, "apiKey")

        // Simulate success state here by asserting the weather data is populated
        // You can check the actual weather data value depending on the implementation
        Mockito.verify(errorStateObserver, Mockito.never()).onChanged(Mockito.anyString())
    }

    @Test
    fun testFetchWeatherByZipCode_error() {
        // Simulate an error scenario
        val zipCode = "invalidZipCode"
        weatherViewModel.fetchWeatherByZip(zipCode, "apiKey")

        // Simulate the error state here, assert the error message is set
        Mockito.verify(errorStateObserver).onChanged(Mockito.anyString())
    }

    @Test
    fun testClearError() {
        // Test clearing the error message
        weatherViewModel.clearError()
        Mockito.verify(errorStateObserver).onChanged("")
    }

    // Add more tests to check other methods like fetching forecast, loading states, etc.
}
