package com.example.appvuelos_capebe.ui.screen

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel

@Composable
fun ReporteDiarioScreen(
    viewModel: CalendarioViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val reporte = state.reporteDiario

    LaunchedEffect(Unit) {
        viewModel.cargarReporteDiario(null)
    }

    Scaffold(
        topBar = { Header("REPORTE DIARIO DGAC / MANTENIMIENTO") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
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
                    Text("Generando reporte oficial...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            // Encabezado oficial del reporte
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "REPORTE DE CIERRE DE ACTIVIDAD DE VUELOS",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Destinatarios oficiales:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("• Director de Operaciones (Bitácora legal exigible por DGAC)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• Director de Mantenimiento (Control estricto de horas de vuelo)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vuelos realizados: ${reporte?.totalVuelosRealizados ?: 0}",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Total horómetro: ${String.format("%.1f", reporte?.totalHorasVoladas ?: 0.0)} hrs",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Estado de Flota y Alerta Legal DGAC
            Text(
                text = "Control de Horas de Flota (Director de Mantenimiento)",
                color = Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            val flota = reporte?.estadoFlotaMantenimiento ?: state.aeronaves
            flota.forEach { avion ->
                val restantes = (avion.horometroMaxMantenimiento - avion.horometroActual).coerceAtLeast(0.0)
                val bloqueada = avion.enMantenimiento || restantes <= 0.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bloqueada) Color(0xFF3A1217) else Color(0xFF13223E)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (bloqueada) Color(0xFFEF4444) else Color(0xFF1E3A5F)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${avion.matricula} • ${avion.modelo}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (bloqueada) "⛔ FUERA DE VUELO" else "✓ OPERATIVA",
                                color = if (bloqueada) Color(0xFFF87171) else Color(0xFF10B981),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Capacidad: ${avion.plazasTotales} plazas (máx. ${avion.asientosDisponibles} pasajeros) • Base: ${avion.baseAerea}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Horómetro: ${String.format("%.1f", avion.horometroActual)} / ${String.format("%.1f", avion.horometroMaxMantenimiento)} hrs • Restante: ${String.format("%.1f", restantes)} hrs",
                            color = if (bloqueada) Color(0xFFFCA5A5) else Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Vuelos clasificados por Matrícula, Piloto, Destino y Hora Local
            Text(
                text = "Detalle de Vuelos Realizados (Bitácora DGAC)",
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            val vuelosRep = reporte?.vuelosRealizados.orEmpty()
            if (vuelosRep.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Text(
                        text = "Aún no hay vuelos cerrados con horómetro registrado hoy.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                vuelosRep.forEach { v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Matrícula: ${v.matricula ?: v.aeronave}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Hora Local: ${v.horaLocal ?: v.horario}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("Piloto al Mando: ${v.pilotoAlMando ?: "Cap. CAPEBE"}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Destino: ${v.destino ?: v.base}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("Fecha: ${v.fecha} • Pasajero: ${v.pasajero ?: "Socio"} (${v.cantidadPasajeros ?: 1} pax)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            if (v.horometroInicial != null && v.horometroFinal != null) {
                                Text(
                                    text = "Horómetro: ${String.format("%.1f", v.horometroInicial)} → ${String.format("%.1f", v.horometroFinal)} hrs (${v.duracionMinutos ?: 18} min)",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Botón para emitir/enviar el reporte oficial a Director de Operaciones y Director de Mantenimiento
            Button(
                onClick = {
                    val cuerpo = buildString {
                        appendLine("REPORTE DIARIO OFICIAL DE VUELOS — CLUB AÉREO CAPEBE")
                        appendLine("Para: Director de Operaciones (DGAC) y Director de Mantenimiento")
                        appendLine("Total vuelos realizados: ${reporte?.totalVuelosRealizados ?: 0}")
                        appendLine("Total horas horómetro: ${String.format("%.1f", reporte?.totalHorasVoladas ?: 0.0)} hrs")
                        appendLine()
                        appendLine("--- DETALLE DE VUELOS (Matrícula / Piloto / Destino / Hora Local) ---")
                        vuelosRep.forEach { v ->
                            appendLine("• [${v.matricula ?: v.aeronave}] ${v.fecha} ${v.horaLocal ?: v.horario} | Piloto: ${v.pilotoAlMando} | Destino: ${v.destino} | Horómetro: ${v.horometroInicial} -> ${v.horometroFinal}")
                        }
                        appendLine()
                        appendLine("--- ESTADO MANTENIMIENTO FLOTA (DGAC) ---")
                        flota.forEach { a ->
                            val estado = if (a.enMantenimiento) "FUERA DE VUELO (MANTENIMIENTO OBLIGATORIO)" else "OPERATIVA"
                            appendLine("• ${a.matricula} (${a.modelo}): ${a.horometroActual}/${a.horometroMaxMantenimiento} hrs - $estado")
                        }
                    }

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Reporte Diario de Vuelos CAPEBE — DGAC y Mantenimiento")
                        putExtra(Intent.EXTRA_TEXT, cuerpo)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Enviar Reporte Diario CAPEBE"))
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
                Text("📤 Emitir Reporte a Dir. Operaciones y Mantenimiento", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onNavigateBack,
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
