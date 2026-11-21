package com.example.appvuelos_capebe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appvuelos_capebe.data.database.AppDatabase
import com.example.appvuelos_capebe.data.local.entity.UsuarioEntity
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Ingreso con PIN consultado desde Room SQLite
@Composable
fun PinScreen(
    onPinSuccess: (String) -> Unit,
    onCambiarUsuario: () -> Unit
) {
    val context = LocalContext.current
    var usuarioSqlite by remember { mutableStateOf<UsuarioEntity?>(null) }
    var pinIngresado by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = Color(0xFF1E3A5F),
        focusedLabelColor = Color(0xFF38BDF8),
        unfocusedLabelColor = Color(0xFF94A3B8),
        cursorColor = Color(0xFF38BDF8)
    )

    LaunchedEffect(Unit) {
        val ultimo = withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(context).usuarioDao().getUltimoUsuario()
        }
        if (ultimo == null) {
            onCambiarUsuario()
        } else {
            usuarioSqlite = ultimo
        }
    }

    val nombreGuardado = usuarioSqlite?.name ?: "Encargado de Pista"
    val pinGuardado = usuarioSqlite?.pin ?: ""

    Scaffold(
        topBar = { Header("INGRESO CON PIN PISTA") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hola, $nombreGuardado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu PIN de 4 dígitos registrado en SQLite",
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pinIngresado,
                        onValueChange = { nuevo ->
                            pinIngresado = nuevo.filter { it.isDigit() }.take(4)
                            mensajeError = null
                        },
                        label = { Text("PIN (4 dígitos)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    if (mensajeError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = mensajeError!!, color = Color(0xFFF87171))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pinIngresado.length != 4) {
                                mensajeError = "El PIN debe tener 4 dígitos."
                            } else if (pinIngresado == pinGuardado) {
                                vibrar(context, 200L)
                                Toast.makeText(context, "Bienvenido $nombreGuardado", Toast.LENGTH_SHORT).show()
                                onPinSuccess(nombreGuardado)
                            } else {
                                mensajeError = "PIN incorrecto."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Entrar con PIN", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onCambiarUsuario() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Ingresar con otra cuenta", color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}
