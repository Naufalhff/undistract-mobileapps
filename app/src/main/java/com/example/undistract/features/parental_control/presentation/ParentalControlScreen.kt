package com.example.undistract.features.parental_control.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.undistract.features.authentication_parental.presentation.AuthenticationViewModel
import com.example.undistract.ui.theme.ColorNew
import kotlinx.coroutines.launch
import com.example.undistract.features.parental_control.DeviceAdminUtils

@Composable
fun ParentalControlScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel,
    pinLength: Int = 6
) {
    val isVerified by viewModel.isVerified.collectAsState()
    val context = LocalContext.current
    var isDeviceAdminActive by remember { mutableStateOf(DeviceAdminUtils.isDeviceAdminActive(context)) }

    LaunchedEffect(isVerified) {
        if (isVerified) {
            navController.navigate("parental_usage_limit?isParental=true") {
                launchSingleTop = true
            }
            viewModel.resetVerificationStatus()
        }
    }

    if (!isVerified) {
        Box(modifier = Modifier.fillMaxSize()) {
            PinVerificationContent(
                viewModel = viewModel,
                pinLength = pinLength,
                onVerificationSuccess = {
                    viewModel.setVerified(true)
                },
                navController = navController,
            )
        }
    }

    Row {
        Text("Proteksi uninstall")
        Switch(
            checked = isDeviceAdminActive,
            onCheckedChange = { checked ->
                if (checked) {
                    DeviceAdminUtils.requestEnableDeviceAdmin(context)
                } else {
                    DeviceAdminUtils.requestDisableDeviceAdmin(context)
                }
                // Perbarui status setelah user kembali ke aplikasi
                isDeviceAdminActive = DeviceAdminUtils.isDeviceAdminActive(context)
            }
        )
    }
}

@Composable
fun PinVerificationContent(
    viewModel: AuthenticationViewModel,
    pinLength: Int = 6,
    onVerificationSuccess: () -> Unit,
    navController: NavController,
) {
    var pin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusRequesters = List(pinLength) { FocusRequester() }

    LaunchedEffect(Unit) {
        pin = ""
        viewModel.resetVerificationStatus()
        viewModel.checkIfPinExists()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val message = when (viewModel.hasPin) {
                    true -> "Masukkan PIN"
                    false -> "Masukkan Email"
                    else -> ""
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (viewModel.hasPin == false) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Button(
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorNew.primary,
                            contentColor = Color.White
                        ),
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.viewModelScope.launch {
                                    viewModel.sendOtp(email)
                                    // viewModel.setTempEmail(email) // (optional) simpan email sementara di ViewModel
                                    navController.navigate("verifyOtp?email=$email")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim OTP")
                    }

                    return@Column // Hentikan rendering PIN field jika belum ada PIN
                }

                // === PIN Input Only Shown if PIN Sudah Ada ===
                LaunchedEffect(pin) {
                    if (pin.length == pinLength) {
                        viewModel.viewModelScope.launch {
                            val isCorrect = viewModel.verifyPin(pin)
                            if (isCorrect) {
                                onVerificationSuccess()
                            } else {
                                pin = ""
                                Toast.makeText(context, "PIN salah", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until pinLength) {
                        val char = if (i < pin.length) pin[i].toString() else ""

                        OutlinedTextField(
                            value = char,
                            onValueChange = { value ->
                                if (value.length <= 1 && value.all { it.isDigit() }) {
                                    var newPin = pin
                                    if (newPin.length > i) {
                                        newPin = newPin.substring(0, i) + value + newPin.substring(i + 1)
                                    } else if (newPin.length == i) {
                                        newPin += value
                                    }
                                    pin = newPin

                                    if (value.isNotEmpty() && i < pinLength - 1) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .focusRequester(focusRequesters[i]),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, lineHeight = 1.em),
                            visualTransformation = PasswordVisualTransformation(),
                            placeholder = { Text("●") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.DarkGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Lupa PIN?",
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            navController.navigate("verifyOtp?email={email}")
                        },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}
