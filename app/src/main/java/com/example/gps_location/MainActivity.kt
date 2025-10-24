package com.example.gps_location
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


data class QueuedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val bearing: Float,
    val time: String
)

class MainActivity : ComponentActivity(), LocationListener {

    private val _locationData = mutableStateOf<Location?>(null)
    private val _locationTracking = mutableStateOf(false)
    private val _locationName = mutableStateOf("")
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val locationQueue = mutableListOf<QueuedLocation>()
    private var isSending = false

    private fun enqueueLocation(location: Location, locname: String) {
        val timeFormatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(location.time))

        val item = QueuedLocation(
            name = locname,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            bearing = location.bearing,
            time = timeFormatted
        )

        locationQueue.add(item)
        Log.d("QUEUE", "Dodano do kolejki (${locationQueue.size}): $item")

        tryToSendQueuedLocations()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val tracking = _locationTracking.value

            LaunchedEffect(tracking) {
                if (tracking) {
                    startLocationUpdates()
                } else {
                    stopLocationUpdates()
                }
            }



        GpsLocationFunctionality(location = _locationData.value,
            locationTracking = _locationTracking.value,
            onLocationTrackingChanged = { _locationTracking.value = it },
            name = _locationName.value,
            onNameChanged = { _locationName.value = it })
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkLocationPermission()
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!_locationTracking.value) return
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000L, // co 5 sekund
            0f,
            this
        )
    }

    private fun checkLocationPermission() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted && coarseLocationGranted) {
            // Masz już uprawnienia – możesz pobierać lokalizację
            Toast.makeText(this, "Uprawnienia lokalizacji przyznane", Toast.LENGTH_SHORT).show()
            startLocationUpdates()
        } else {
            // Poproś użytkownika o uprawnienia
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        _locationData.value = location
        val locname = _locationName.value

        if (locname.isNotEmpty()) {
            enqueueLocation(location, locname)
        }
    }

    private fun sendLocationToServer(
        item: QueuedLocation,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "http://gpslocation.fcomms.website/api.php"
        val volleyQueue = Volley.newRequestQueue(this)

        val params = HashMap<String, String>().apply {
            put("name", item.name)
            put("latitude", item.latitude.toString())
            put("longitude", item.longitude.toString())
            put("altitude", item.altitude.toString())
            put("bearing", item.bearing.toString())
            put("time", item.time)
        }

        val request = object : StringRequest(
            Method.POST, url,
            {
                onSuccess()
            },
            { error ->
                onError(error.message ?: "Nieznany błąd")
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
            override fun getBodyContentType(): String =
                "application/x-www-form-urlencoded; charset=UTF-8"
        }

        volleyQueue.add(request)
    }
    private fun tryToSendQueuedLocations() {
        if (isSending || locationQueue.isEmpty()) return
        isSending = true

        fun sendNext() {
            if (locationQueue.isEmpty()) {
                isSending = false
                Log.d("QUEUE", "Wszystkie dane z kolejki wysłane.")
                return
            }

            val item = locationQueue.first()

            sendLocationToServer(item,
                onSuccess = {
                    Log.d("QUEUE_SEND", "Wysłano: ${item.name}")
                    locationQueue.removeAt(0)
                    sendNext() // wysyłaj kolejny
                },
                onError = { error ->
                    Log.e("QUEUE_ERROR", "Błąd przy wysyłce: $error")
                    isSending = false // zatrzymaj i spróbuj później
                }
            )
        }

        sendNext()
    }



    // Obsługa odpowiedzi użytkownika
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Uprawnienia przyznane!", Toast.LENGTH_SHORT).show()
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Odmówiono uprawnień do lokalizacji", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @Composable
    fun GpsLocationFunctionality(location: Location?,
                                 locationTracking: Boolean,
                                 onLocationTrackingChanged: (Boolean) -> Unit,
                                 name: String,
                                 onNameChanged: (String) -> Unit)
    {
        Column (verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize().background(color = Color.DarkGray).padding(20.dp)) {
            Row (modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Column {
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        //Nazwa

                        Text(
                            text = "Location Name",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Row (modifier = Modifier.fillMaxWidth()) {
                        //Pole Nazwy
                        OutlinedTextField(value = name,
                            onValueChange = {onNameChanged(it)},
                            placeholder = { Text("Name Here..") }, singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = Color(10, 130, 220),
                                unfocusedBorderColor = Color.White,
                                cursorColor = Color.White,
                                unfocusedPlaceholderColor = Color.Gray,
                                focusedPlaceholderColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                )
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Szerokość
                        Text(text = "Lat: ${location?.latitude ?: "Brak"}", color = Color.White)
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Długość
                        Text(text = "Lon: ${location?.longitude ?: "Brak"}", color = Color.White)
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Wysokość
                        Text(text = "Altitude: ${location?.altitude ?: "Brak"}", color = Color.White)
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Kierunek
                        Text(text = "Bearing: ${location?.bearing ?: "Brak"}°", color = Color.White)
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Dokładność
                        Text(text = "Accuracy: ${location?.accuracy ?: "Brak"}", color = Color.White)
                    }
                    Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        //Czas
                        val time = location?.time?.let { java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(it)) } ?: "Brak"
                        Text(text = "Time: $time", color = Color.White)
                    }
                    Divider(modifier = Modifier.padding(top = 5.dp, bottom = 20.dp))

                }

            }
            Row (modifier = Modifier.fillMaxWidth()) {

            }
            Row (modifier = Modifier.fillMaxWidth()) {

            }
            Row ( horizontalArrangement = Arrangement.Center,modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {

                Button(
                    onClick = {onLocationTrackingChanged(!locationTracking)},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (locationTracking) {Color(10, 130, 220)}
                        else Color.Gray,
                        ),
                    modifier = Modifier.height(60.dp).width(200.dp)) {
                Text(text = if (locationTracking) {"STOP"} else "START",
                    color = if (locationTracking)
                    {Color.White} else Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

            }
        }
    }


}







