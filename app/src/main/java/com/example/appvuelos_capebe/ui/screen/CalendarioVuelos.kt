package com.example.appvuelos_capebe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appvuelos_capebe.ui.components.CalendarioMensualPista
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.GoogleCalendarHelper
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel

@Composable
fun CalendarioVuelos(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmacion: () -> Unit,
    viewModel: CalendarioViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.limpiarFormularioOperacion()
        viewModel.cargarDesdeMicroservicios()
    }

    val vuelosFiltrados = if (state.fechaFiltro != null) {
        state.vuelos.filter { it.fecha == state.fechaFiltro.toString() }
    } else {
        state.vuelos
    }

    Scaffold(
        topBar = { Header("CALENDARIO Y CONTROL DE PISTA") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendario Operativo (Sáb/Dom/Festivos)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = if (state.conectadoPista || state.conectadoQr) "🔄 ☁ Sync OK" else "🔄 Reintentar",
                    color = if (state.conectadoPista || state.conectadoQr) Color(0xFF10B981) else Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.cargarDesdeMicroservicios() }
                )
            }

            CalendarioMensualPista(
                fechaSeleccionada = state.fechaFiltro,
                fechasConVuelos = state.vuelos.map { it.fecha }.toSet(),
                onFechaSeleccionada = { viewModel.onFechaFiltroChanged(it) }
            )

            if (state.cargando) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Sincronizando vuelos y QRs...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            state.mensajeServidor?.let { aviso ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text(text = aviso, color = Color(0xFFFCA5A5), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (vuelosFiltrados.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Text(
                        text = "No hay vuelos programados para ${state.fechaFiltro}. Toca 'Ver todos' o valida un código QR directamente.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                vuelosFiltrados.forEach { vuelo ->
                    val seleccionado = state.vueloElegido?.codigoQr == vuelo.codigoQr
                    val habilitado = vuelo.estadoPista.contains("HABILITADO", ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (habilitado) {
                                    Toast.makeText(
                                        context,
                                        "El vuelo ${vuelo.codigoQr} ya fue habilitado en pista",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    vibrar(context, 120L)
                                    viewModel.onVueloElegido(vuelo)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado) Color(0xFF1E3A5F) else Color(0xFF13223E)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (seleccionado) Color(0xFF38BDF8) else Color(0xFF1E3A5F)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${vuelo.fecha} • ${vuelo.horario}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = vuelo.codigoQr,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pasajero: ${vuelo.pasajero} (${if (vuelo.esSocio) "Socio" else "Invitado"} • ${vuelo.cantidadPasajeros} pax)",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Base: ${vuelo.base} • ${vuelo.aeronave}",
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = vuelo.estadoPista,
                                    color = if (habilitado) Color(0xFF10B981) else Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "📅 Google Calendar",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        GoogleCalendarHelper.agregarVueloAGoogleCalendar(
                                            context = context,
                                            fechaIso = vuelo.fecha,
                                            bloqueHorario = vuelo.horario,
                                            aeronave = vuelo.aeronave,
                                            baseAerea = vuelo.base,
                                            pasajero = vuelo.pasajero,
                                            codigoVerificacion = vuelo.codigoQr
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val seleccion = state.vueloElegido

            if (seleccion != null) {
                Button(
                    onClick = {
                        vibrar(context, 250L)
                        onNavigateToConfirmacion()
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
                    Text(
                        text = "Validar Vuelo (${seleccion.codigoQr}) y Horómetro",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    vibrar(context, 200L)
                    viewModel.limpiarFormularioOperacion()
                    onNavigateToConfirmacion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF070D19)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "📷 Escanear / Validar Nuevo Código QR",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OutlinedButton(
                onClick = { onNavigateBack() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Volver al Menú", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            }
        }
    }
}
