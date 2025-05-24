package com.example.undistract.features.authentication_parental.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun VerifyOTPScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel,
    email: String,
    otpLength: Int = 6
) {
    var otp by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    var currentEmail by remember { mutableStateOf(email) }

    LaunchedEffect(viewModel.hasPin) {
        if (viewModel.hasPin == true) {
            viewModel.viewModelScope.launch {
                val userEmail = viewModel.getEmail()
                currentEmail = userEmail ?: ""
                viewModel.sendOtp(currentEmail)
            }
        }
    }

    LaunchedEffect(otp) {
        if (otp.length == otpLength) {
            viewModel.viewModelScope.launch {
                val isCorrect = viewModel.verifyOtp(currentEmail, otp)
                if (isCorrect) {
                    Toast.makeText(context, "Verifikasi berhasil", Toast.LENGTH_SHORT).show()
                    navController.navigate("createPin?email=$currentEmail") {
                        launchSingleTop = true
                    }
                } else {
                    otp = ""
                    Toast.makeText(context, "Kode OTP salah", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                Text(
                    text = "Masukkan Kode OTP",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val maskedEmail = remember(currentEmail) { maskEmail(currentEmail) }

                Text(
                    text = "Kode OTP telah dikirimkan ke email $maskedEmail",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until otpLength) {
                        val char = if (i < otp.length) otp[i].toString() else ""

                        OutlinedTextField(
                            value = char,
                            onValueChange = { value ->
                                if (value.length <= 1 && value.all { it.isDigit() }) {
                                    var newOtp = otp
                                    if (newOtp.length > i) {
                                        newOtp = newOtp.substring(0, i) + value + newOtp.substring(i + 1)
                                    } else if (newOtp.length == i) {
                                        newOtp += value
                                    }
                                    otp = newOtp

                                    if (value.isNotEmpty() && i < otpLength - 1) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .focusRequester(focusRequesters[i]),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
    }
}

fun maskEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2 || parts[0].length < 3) return email

    val name = parts[0]
    val maskedName = name.take(2) + "*".repeat(name.length - 3) + name.takeLast(1)
    return "$maskedName@${parts[1]}"
}
