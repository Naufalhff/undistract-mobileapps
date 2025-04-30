package com.example.undistract.features.parental_control.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.undistract.features.parental_control.data.ParentalControlRepository
import kotlinx.coroutines.launch

@Composable
fun PinVerificationScreen(
    navController: NavHostController,
    viewModel: ParentalControlViewModel
) {
    var isPinVerified by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkIfPinExists()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                viewModel.checkIfPinExists()
                Log.d("DEBUG", "${viewModel.hasPin}")
                val message = when (viewModel.hasPin) {
                    true -> "Masukkan PIN"
                    false -> "Masukkan PIN baru"
                    else -> ""
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PinInput(
                    initialPin = pin,
                    onPinChanged = { pin = it },
                    onPinComplete = { enteredPin ->
                        if (viewModel.hasPin == true) {
                            viewModel.viewModelScope.launch {
                                val isCorrect = viewModel.verifyPin(enteredPin)
                                if (isCorrect) {
                                    isPinVerified = true
                                    navController.navigate("parental_control")
                                } else {
                                    pin = ""
                                    Toast.makeText(context, "PIN salah", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            viewModel.viewModelScope.launch {
                                viewModel.addPin(enteredPin)
                                navController.navigate("pin_verification")
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun PinInput(
    length: Int = 6,
    initialPin: String,
    onPinChanged: (String) -> Unit,
    onPinComplete: (String) -> Unit
) {
    val focusRequesters = List(length) { FocusRequester() }

    LaunchedEffect(initialPin) {
        if (initialPin.length == length) {
            onPinComplete(initialPin)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (i in 0 until length) {
            val char = if (i < initialPin.length) initialPin[i].toString() else ""

            OutlinedTextField(
                value = char,
                onValueChange = { value ->
                    if (value.length <= 1 && value.all { it.isDigit() }) {
                        var newPin = initialPin
                        if (newPin.length > i) {
                            newPin = newPin.substring(0, i) + value + newPin.substring(i + 1)
                        } else if (newPin.length == i) {
                            newPin += value
                        }
                        onPinChanged(newPin)

                        if (value.isNotEmpty() && i < length - 1) {
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
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}



//class FakeParentalControlViewModel : ParentalControlViewModel(null) {
//    override var hasPin: Boolean? by mutableStateOf(false)
//
//    override fun checkIfPinExists() {
//        hasPin = false // Dummy logic
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PinVerificationScreenPreview() {
//    val navController = rememberNavController()
//    PinVerificationScreen(navController = navController, viewModel = FakeParentalControlViewModel())
//}