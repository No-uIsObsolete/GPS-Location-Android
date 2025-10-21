package com.example.gps_location
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity(), LocationListener {

    private val _locationData = mutableStateOf<Location?>(null)
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
        GpsLocationFunctionality(location = _locationData.value)
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000L, // co 5 sekund
            1f, // co 1 metr
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
    fun GpsLocationFunctionality(location: Location?)
    {
        var location_tracking by remember { mutableStateOf(false) }
        var name by remember {mutableStateOf("")}
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
                            onValueChange = {name = it},
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
                    onClick = {location_tracking = !location_tracking},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (location_tracking == true) {Color(10, 130, 220)}
                        else Color.Gray,
                        ),
                    modifier = Modifier.height(60.dp).width(200.dp)) {
                Text(text = if (location_tracking == true) {"STOP"} else "START", color = if (location_tracking == true) {Color.White} else Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

            }
        }
    }


}







