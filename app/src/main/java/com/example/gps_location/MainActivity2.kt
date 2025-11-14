package com.example.gps_location

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


data class LoginResult(
    val success: Boolean,
    val message: String,
    val username: String? = null,
    val email: String? = null,
    val id: String? = null
)
class MainActivity2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("logged_in", false)

        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            LoginUi(
                email = email,
                onEmailChanged = { email = it },
                password = password,
                onPasswordChanged = { password = it },
                isLoading = isLoading,
                onLoginClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = loginUser(email, password) // Zmieniona funkcja
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                if (result.success) {
                                    // Zapisz dane użytkownika do SharedPreferences
                                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    prefs.edit().apply {
                                        putBoolean("logged_in", true)
                                        putString("id", result.id)
                                        putString("username", result.username)
                                        putString("email", result.email)
                                    }.apply()

                                    Toast.makeText(this@MainActivity2, "Zalogowano jako ${result.username}", Toast.LENGTH_SHORT).show()

                                    // Przejdź do MainActivity
                                    val intent = Intent(this@MainActivity2, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Pokazanie komunikatu o błędzie
                                    Toast.makeText(this@MainActivity2, result.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    suspend fun loginUser(email: String, password: String): LoginResult {
        val url = "https://gpslocation.fcomms.website/api/loginAndroid.php"
        val params = mapOf(
            "email" to email,
            "password" to password
        )

        // Zrób zapytanie do API
        return withContext(Dispatchers.IO) {
            try {
                val response = apiRequest(url, params)
                if (response.getBoolean("success")) {
                    val user = response.getJSONObject("user")
                    LoginResult(
                        success = true,
                        message = "Zalogowano pomyślnie",
                        username = user.getString("username"),
                        email = user.getString("email"),
                        id = user.getString("id")
                    )
                } else {
                    LoginResult(
                        success = false,
                        message = response.getString("message")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoginResult(success = false, message = "Błąd połączenia z serwerem")
            }
        }
    }

    fun apiRequest(url: String, params: Map<String, String>): JSONObject {
        val urlObj = URL(url)
        val connection = urlObj.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        // Przygotowanie danych
        val postData = StringBuilder()
        for ((key, value) in params) {
            if (postData.isNotEmpty()) postData.append("&")
            postData.append(key).append("=").append(value)
        }

        // Wysłanie danych
        val outputStream = connection.outputStream
        outputStream.write(postData.toString().toByteArray())
        outputStream.flush()
        outputStream.close()

        // Odczyt odpowiedzi
        val inputStreamReader = InputStreamReader(connection.inputStream)
        val response = inputStreamReader.readText()
        inputStreamReader.close()

        return JSONObject(response)
    }

    @Composable
    fun LoginUi(
        email: String,
        onEmailChanged: (String) -> Unit,
        password: String,
        onPasswordChanged: (String) -> Unit,
        isLoading: Boolean,
        onLoginClick: () -> Unit
    ) {

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().background(color = Color.DarkGray).padding(20.dp)
        ) {

            Row() {

            }

            Row() {
                Column() {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )
                    }


                    Row(modifier = Modifier.fillMaxWidth().padding(top = 30.dp)) {
                        //Email

                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        //Pole Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChanged,
                            singleLine = true,
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
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        //Password

                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        //Pole Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChanged,
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
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
                }
            }

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { if (!isLoading) onLoginClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(10, 130, 220),
                    ),
                    modifier = Modifier.height(60.dp).width(200.dp)
                ) {
                    if (isLoading)
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    else
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                }
            }
            Row() {

            }
        }
    }

}
