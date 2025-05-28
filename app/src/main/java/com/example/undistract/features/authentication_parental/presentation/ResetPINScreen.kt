package com.example.undistract.features.authentication_parental.presentation

import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.undistract.ui.theme.ColorNew
import kotlinx.coroutines.launch

@Composable
fun ResetPINScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel,
    pinLength: Int = 6
) {
    val context = LocalContext.current

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    val oldPinFocusRequesters = remember { List(pinLength) { FocusRequester() } }
    val newPinFocusRequesters = remember { List(pinLength) { FocusRequester() } }

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
                    text = "Reset PIN",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(text = "Masukkan PIN Lama", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until pinLength) {
                        val char = if (i < oldPin.length) oldPin[i].toString() else ""
                        OutlinedTextField(
                            value = char,
                            onValueChange = { value ->
                                if (value.length <= 1 && value.all { it.isDigit() }) {
                                    var newValue = oldPin
                                    if (newValue.length > i) {
                                        newValue = newValue.substring(0, i) + value + newValue.substring(i + 1)
                                    } else if (newValue.length == i) {
                                        newValue += value
                                    }
                                    oldPin = newValue
                                    if (value.isNotEmpty() && i < pinLength - 1) {
                                        oldPinFocusRequesters[i + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .focusRequester(oldPinFocusRequesters[i]),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Masukkan PIN Baru", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until pinLength) {
                        val char = if (i < newPin.length) newPin[i].toString() else ""
                        OutlinedTextField(
                            value = char,
                            onValueChange = { value ->
                                if (value.length <= 1 && value.all { it.isDigit() }) {
                                    var newValue = newPin
                                    if (newValue.length > i) {
                                        newValue = newValue.substring(0, i) + value + newValue.substring(i + 1)
                                    } else if (newValue.length == i) {
                                        newValue += value
                                    }
                                    newPin = newValue
                                    if (value.isNotEmpty() && i < pinLength - 1) {
                                        newPinFocusRequesters[i + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .focusRequester(newPinFocusRequesters[i]),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorNew.primary,
                        contentColor = Color.White
                    ),
                    onClick = {
                        viewModel.viewModelScope.launch {
                            val isOldPinCorrect = viewModel.verifyPin(oldPin)
                            if (isOldPinCorrect) {
                                viewModel.updatePin(newPin)
                                Toast.makeText(context, "PIN berhasil diubah", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                oldPin = ""
                                newPin = ""
                                Toast.makeText(context, "PIN lama salah", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan PIN Baru")
                }
            }
        }
    }
}
