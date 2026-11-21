package com.example.appvuelos_capebe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appvuelos_capebe.ui.components.CodigoQrVista
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel

@Composable
fun ConfirmacionVuelo(
    nombreSocio: String,
    viewModel: CalendarioViewModel,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val vuelo = remember { state.vueloElegido } ?: state.vueloElegido
    val pilotoMostrar = remember { state.pilotoAlMando }
    val horoIniMostrar = remember { state.horometroInicialTexto }
    val horoFinMostrar = remember { state.horometroFinalTexto }
    val obsMostrar = remember { state.observaciones }

    Scaffold(
        topBar = { Header("HABILITACIÓN DE PISTA Y HORÓMETRO") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", fontSize = 32.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Encargado: $nombreSocio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (vuelo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AUTORIZACIÓN OFICIAL DGAC", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("☁ REST API OK", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("${vuelo.fecha} • ${vuelo.horario}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text("Pasajero: ${vuelo.pasajero} (${vuelo.rut})", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                        Text("Base: ${vuelo.base} • ${vuelo.aeronave}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Piloto al Mando: $pilotoMostrar", color = Color(0xFF38BDF8), fontSize = 13.sp)
                        Text(
                            text = "Horómetro: $horoIniMostrar → $horoFinMostrar hrs",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Observaciones: $obsMostrar", color = Color(0xFFCBD5E1), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        CodigoQrVista(codigo = vuelo.codigoQr, tamano = 120.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    vibrar(context, 450L)
                    viewModel.guardarVueloConfirmado(nombreSocio) { sincronizado, errorMsg ->
                        val msg = when {
                            sincronizado -> "¡QR validado y horómetro guardado en PostgreSQL!"
                            errorMsg != null -> "Guardado local (Aviso servidor: $errorMsg)"
                            else -> "QR validado y habilitado (modo local)"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        onNavigateHome()
                    }
                },
                enabled = !state.cargando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (state.cargando) "Registrando en Spring Boot..." else "Confirmar y Registrar Horómetro",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
