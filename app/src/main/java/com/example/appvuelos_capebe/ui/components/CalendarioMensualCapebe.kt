package com.example.appvuelos_capebe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val FERIADOS_CHILE_PISTA = setOf(
    "2026-11-01", "2026-12-08", "2026-12-25",
    "2027-01-01", "2027-05-01", "2027-05-21", "2027-09-18", "2027-09-19"
)

fun esDiaOperacionPista(fecha: LocalDate): Boolean {
    return fecha.dayOfWeek == DayOfWeek.SATURDAY
            || fecha.dayOfWeek == DayOfWeek.SUNDAY
            || FERIADOS_CHILE_PISTA.contains(fecha.toString())
}

private fun nombreMesPista(mes: Int): String = when (mes) {
    1 -> "Enero"; 2 -> "Febrero"; 3 -> "Marzo"; 4 -> "Abril"
    5 -> "Mayo"; 6 -> "Junio"; 7 -> "Julio"; 8 -> "Agosto"
    9 -> "Septiembre"; 10 -> "Octubre"; 11 -> "Noviembre"; else -> "Diciembre"
}

@Composable
fun CalendarioMensualPista(
    fechaSeleccionada: LocalDate?,
    fechasConVuelos: Set<String> = emptySet(),
    onFechaSeleccionada: (LocalDate?) -> Unit
) {
    val referencia = fechaSeleccionada ?: LocalDate.of(2026, 11, 21)
    var mesActual by remember(referencia.year, referencia.monthValue) {
        mutableStateOf(YearMonth.of(referencia.year, referencia.month))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                    Text("‹ Ant", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${nombreMesPista(mesActual.monthValue)} ${mesActual.year}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                TextButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                    Text("Sig ›", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEachIndexed { idx, dia ->
                    Text(
                        text = dia,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = if (idx >= 5) Color(0xFF38BDF8) else Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val primerDia = mesActual.atDay(1)
            val desplazamiento = primerDia.dayOfWeek.value - 1
            val diasMes = mesActual.lengthOfMonth()
            val totalCeldas = ((desplazamiento + diasMes + 6) / 7) * 7

            for (fila in 0 until (totalCeldas / 7)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..6) {
                        val idx = fila * 7 + col
                        val diaNum = idx - desplazamiento + 1
                        if (diaNum in 1..diasMes) {
                            val fecha = mesActual.atDay(diaNum)
                            val habilitado = esDiaOperacionPista(fecha)
                            val esFestivo = FERIADOS_CHILE_PISTA.contains(fecha.toString())
                            val seleccionada = fecha == fechaSeleccionada
                            val tieneVuelo = fechasConVuelos.contains(fecha.toString())

                            val fondo = when {
                                seleccionada -> Color(0xFF0284C7)
                                esFestivo -> Color(0xFF78350F)
                                habilitado -> Color(0xFF1E3A5F)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(fondo)
                                    .then(
                                        if (seleccionada) {
                                            Modifier.border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                        } else Modifier
                                    )
                                    .clickable {
                                        if (seleccionada) onFechaSeleccionada(null) else onFechaSeleccionada(fecha)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$diaNum",
                                        color = when {
                                            seleccionada -> Color.White
                                            esFestivo -> Color(0xFFFBBF24)
                                            habilitado -> Color.White
                                            else -> Color(0xFF475569)
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (habilitado || seleccionada) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (tieneVuelo) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).height(36.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (fechaSeleccionada != null) "Filtrando: $fechaSeleccionada" else "Mostrando todos los vuelos",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                if (fechaSeleccionada != null) {
                    Text(
                        text = "Ver todos",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onFechaSeleccionada(null) }
                    )
                }
            }
        }
    }
}
