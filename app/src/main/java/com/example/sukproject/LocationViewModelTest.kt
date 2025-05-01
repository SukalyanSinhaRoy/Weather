import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.sukproject.LocationService
import com.example.sukproject.WeatherResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat

class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationService: LocationService
    private lateinit var locationObserver: Observer<WeatherResponse?>

    @Before
    fun setUp() {
        locationService = mock(LocationService::class.java)
        locationViewModel = LocationViewModel(mock())
        locationObserver = mock()
        locationViewModel.locationBasedWeather.observeForever(locationObserver)
    }

    @Test
    fun `test bindLocationService`() {
        locationViewModel.bindLocationService()

        verify(locationService, times(1)).startService(any())
        verify(locationService, times(1)).bindService(any(), any(), any())
    }

    @Test
    fun `test getCurrentCoordinates with null location`() {
        `when`(locationService.getLastLocation(any())).thenReturn(null)

        locationViewModel.getCurrentCoordinates { lat, lon ->
            assert(lat == null)
            assert(lon == null)
        }
    }

    @Test
    fun `test getCurrentCoordinates with valid location`() {
        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(45.0)
        `when`(location.longitude).thenReturn(-75.0)
        `when`(locationService.getLastLocation(any())).thenReturn(location)

        locationViewModel.getCurrentCoordinates { lat, lon ->
            assert(lat == 45.0)
            assert(lon == -75.0)
        }
    }

    @Test
    fun `test clearError`() {
        locationViewModel.clearError()
        assert(locationViewModel.errorMessage.value == null)
    }
}
