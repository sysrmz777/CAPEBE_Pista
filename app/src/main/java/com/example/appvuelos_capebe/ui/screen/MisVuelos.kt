package com.example.appvuelos_capebe.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel

@Composable
fun MisVuelosScreen(
    viewModel: CalendarioViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.cargarDesdeMicroservicios()
    }

    val state = viewModel.uiState
    val vuelosGuardados = state.vuelosGuardados

    Scaffold(
        topBar = { Header("BITÁCORA DE QR Y PISTA (REST)") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.cargando) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Consultando service-qr...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            if (vuelosGuardados.isEmpty() && !state.cargando) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("No hay códigos QR validados en pista aún.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Valida un pase QR y registra el horómetro desde Control de Pista.", color = Color(0xFF38BDF8), fontSize = 13.sp)
                    }
                }
            } else {
                vuelosGuardados.forEach { vuelo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(vuelo.codigoQr, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 15.sp)
                                Text(
                                    text = if (state.conectadoQr) "☁ PostgreSQL OK" else "Registro Local",
                                    color = if (state.conectadoQr) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${vuelo.fecha} • ${vuelo.horario}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Pasajero: ${vuelo.pasajero} (${vuelo.rut.ifBlank { "S/R" }}) • Socio: ${if (vuelo.esSocio) "SÍ" else "NO"}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("Aeronave: ${vuelo.aeronave} • Piloto: ${vuelo.pilotoAlMando}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Base Aérea: ${vuelo.base}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            if (vuelo.horometroInicial != null && vuelo.horometroFinal != null) {
                                Text(
                                    text = "Horómetro: ${String.format("%.1f", vuelo.horometroInicial)} → ${String.format("%.1f", vuelo.horometroFinal)} hrs",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            if (vuelo.observaciones.isNotBlank()) {
                                Text("Observaciones: ${vuelo.observaciones}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Estado: ${vuelo.estadoPista}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onNavigateBack() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Volver al Menú", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            }
        }
    }
}
