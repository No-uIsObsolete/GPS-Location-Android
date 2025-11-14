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
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
                            val result = loginUser(email, password)
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                if (result.success) {
                                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    prefs.edit {
                                        putBoolean("logged_in", true)
                                            .putString("id", result.id)
                                            .putString("username", result.username)
                                            .putString("email", result.email)
                                    }

                                    Toast.makeText(this@MainActivity2, "Zalogowano jako ${result.username}", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@MainActivity2, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
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

    private fun loginUser(email: String, password: String): LoginResult {
        try {
            val url = URL("https://gpslocation.fcomms.website/api/loginAndroid.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doInput = true
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val jsonData = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()

            val outputStream = connection.outputStream
            outputStream.write(jsonData.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val success = json.optBoolean("success", false)
                return if (success) {
                    val user = json.optJSONObject("user")
                    val id = user?.optString("id")
                    val username = user?.optString("username")
                    val emailResp = user?.optString("email")
                    LoginResult(true, "Zalogowano", username, emailResp, id)
                } else {
                    val message = json.optString("message", "Niepoprawne dane logowania")
                    LoginResult(false, message)
                }
            } else {
                return LoginResult(false, "Błąd serwera ($responseCode)")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoginResult(false, "Błąd: ${e.message}")
        }
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