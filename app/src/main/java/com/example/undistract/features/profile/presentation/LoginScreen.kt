package com.example.undistract.features.profile.presentation


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.undistract.MainActivity
import com.example.undistract.config.AppDatabase
import com.example.undistract.core.ApiAuthClient
import com.example.undistract.core.ApiBackendClient
import com.example.undistract.core.ApiService
import com.example.undistract.core.SyncRepository
import com.example.undistract.core.SyncViewModel
import com.example.undistract.features.profile.AuthActivity
import com.example.undistract.features.profile.data.AuthRepository
import com.example.undistract.features.profile.data.AuthViewModel
import com.example.undistract.features.profile.data.AuthViewModelFactory

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var btnSwitch by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    val logo: Painter = painterResource(id = com.example.undistract.R.drawable.app_logo)
    val visibility: Painter = painterResource(id = com.example.undistract.R.drawable.visibility_icon)
    val visibilityOff: Painter = painterResource(id = com.example.undistract.R.drawable.visibilityoff_icon)

    val apiService = ApiAuthClient.create(context)
    val repository = AuthRepository(apiService, context)
    val factory = remember { AuthViewModelFactory(repository) }
    val viewModel: AuthViewModel = viewModel(factory = factory)
    val loginResult by viewModel.loginResult.collectAsState()
    val registerResult by viewModel.registerResult.collectAsState()

    val syncRepository = SyncRepository(
        AppDatabase.getDatabase(context).blockSchedulesDao(),
        AppDatabase.getDatabase(context).variableSessionDao(),
        AppDatabase.getDatabase(context).blockPermanentDao(),
        AppDatabase.getDatabase(context).setaDailyLimitDao(),
        ApiBackendClient.apiService,
        context
    )
    val syncViewModel = SyncViewModel(syncRepository)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(102,42,178)),
        contentAlignment = Alignment.Center,
    ) {

        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            ) {
                if (btnSwitch) {
                    Button(
                        onClick = {btnSwitch = true},
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp, bottom = 2.dp, end = 4.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(102,42,178),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Login")
                    }

                    Button(
                        onClick = {btnSwitch = false; email = ""; password = ""},
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp, bottom = 2.dp, end = 4.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Register")
                    }
                } else {
                    Button(
                        onClick = {btnSwitch = true; email = ""; password = ""},
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp, bottom = 2.dp, end = 4.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Login")
                    }

                    Button(
                        onClick = {btnSwitch = false},
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp, bottom = 2.dp, end = 4.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(102,42,178),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Register")
                    }
                }


            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (!btnSwitch) {

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.Gray
                        )
                    )
                }
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.Gray
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image: Painter = if (passwordVisible) visibility else visibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = image, contentDescription = "Toggle password visibility")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (btnSwitch) viewModel.login(email, password)
                            else viewModel.register(username, email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(102, 42, 178)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (btnSwitch) "Login" else "Register")
                    }

                    loginResult?.let { result ->
                        LaunchedEffect(result) {
                            val text = if (result) "Login sukses!" else "Login gagal!"
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

                            if (result) {
                                val intent = Intent(context, MainActivity::class.java)
                                syncViewModel.fetchAllData(context)
                                context.startActivity(intent)
                            }
                        }
                    }

                    registerResult?.let { result ->
                        LaunchedEffect(result) {
                            val text = if (result) "Register sukses!" else "Register gagal!"
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

                            if (result){
                                btnSwitch = true
                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = logo,
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                )
                Text(
                    text = "Undistract",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 18.dp, start = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Get Started Now",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 14.dp, start = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create account or login",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                    )
                )
            }
        }

    }
}


