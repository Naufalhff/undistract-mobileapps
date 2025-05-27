package com.example.undistract.features.authentication_parental.presentation

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun CreatePINScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel,
    email: String,
    pinLength: Int = 6
) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }

    LaunchedEffect(pin) {
        if (pin.length == pinLength) {
            viewModel.viewModelScope.launch {
                viewModel.addPin(pin, email)
                navController.navigate("parentalControl")
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
                    text = "Masukkan PIN Baru",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            visualTransformation = PasswordVisualTransformation(),
                            placeholder = { Text("●") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
    }
}
