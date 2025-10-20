package com.example.gps_location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpsLocationFunctionality()

        }
    }
}

@Composable
fun GpsLocationFunctionality()
{
    //Manifest.permission.ACCESS_FINE_LOCATION

    //android.location.LocationManager

    var location_tracking by remember { mutableStateOf(false) }
    var name = ""
    var geo_length = 0
    var geo_width = 0
    var geo_altitude = 0
    var timestamp = ""
    var accuracy = 0
    Column (verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize().background(color = Color.DarkGray).padding(20.dp)) {
        Row (modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 10.dp)) {
            Column {
                Row (modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 5.dp)) {
                    //Nazwa
                    Text(text = "Location Name: "+name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
                Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    //Szerokość
                    Text(text = "Lat: "+geo_width, color = Color.White)
                }
                Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    //Długość
                    Text(text = "Lon: "+geo_length, color = Color.White)
                }
                Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    //Wysokość
                    Text(text = "Altitude: "+geo_altitude, color = Color.White)
                }
                Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    //Czas
                    Text(text = "Timestamp: "+timestamp, color = Color.White)
                }
                Row (modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                    //Dokładność
                    Text(text = "Accuracy: "+accuracy, color = Color.White)
                }
                Divider(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))

            }

        }
        Row (modifier = Modifier.fillMaxWidth()) {

        }
        Row (modifier = Modifier.fillMaxWidth()) {

        }
        Row ( horizontalArrangement = Arrangement.End,modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp, end = 10.dp)) {
            IconButton(
                onClick = {location_tracking = !location_tracking},
                modifier = Modifier
                    .size(60.dp)
                    .background
                        (
                        color = if (location_tracking == true) {Color(10, 130, 220)} else Color.Gray,
                        shape = CircleShape
                                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "SendLocation",
                    tint = if (location_tracking == true) {Color.White} else Color.LightGray)

            }
        }
    }
}

