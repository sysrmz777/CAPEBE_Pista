package com.example.appvuelos_capebe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header

@Composable
fun Principal(
    nombreSocio: String = "Encargado Pista",
    onNavigateToMisVuelos: () -> Unit = {},
    onNavigateToPlanesVuelo: () -> Unit = {},
    onNavigateToReporte: () -> Unit = {},
    onNavigateToRecursos: () -> Unit = {},
    onLogout: () -> Unit = {},
    onCerrarSesionCompleta: () -> Unit = onLogout
) {
    val context = LocalContext.current

    val colorCabina = Color(0xFF0A192F)
    val colorPrimario = Color(0xFF1E3A5F)
    val colorCeleste = Color(0xFF38BDF8)
    val colorFondo = Color(0xFF070D19)
    val colorTarjeta = Color(0xFF13223E)
    val colorBorde = Color(0xFF1E3A5F)

    Scaffold(
        topBar = { Header("CENTRO DE CONTROL DE PISTA") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Credencial Encargado de Pista
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colorCeleste.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colorCabina, colorPrimario)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✈️ CREDENCIAL DE LOSA • PISTA ACTIVA",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "SCTB / SCIE",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Encargado de Pista: $nombreSocio",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pistas a cargo: Aeródromo Tobalaba • Carriel Sur Talcahuano",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Text(
                text = "Panel de Operaciones en Losa",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorCeleste,
                modifier = Modifier.padding(top = 4.dp)
            )

            TarjetaMenu(
                icono = "🛫",
                titulo = "Habilitación de Vuelos en Pista",
                subtitulo = "Escanea el QR del socio, valida checklist y autoriza la salida",
                colorAcento = colorCeleste,
                colorFondoTarjeta = colorTarjeta,
                colorBorde = colorBorde,
                onClick = onNavigateToPlanesVuelo
            )

            TarjetaMenu(
                icono = "📘",
                titulo = "Bitácora de Vuelos y QR Habilitados",
                subtitulo = "Registro sincronizado con PostgreSQL de aeronaves autorizadas",
                colorAcento = Color(0xFFF59E0B),
                colorFondoTarjeta = colorTarjeta,
                colorBorde = colorBorde,
                onClick = onNavigateToMisVuelos
            )

            TarjetaMenu(
                icono = "📊",
                titulo = "Reporte Diario DGAC y Mantenimiento",
                subtitulo = "Cierre diario por matrícula, piloto, horómetro y alertas de mantenimiento",
                colorAcento = Color(0xFFA855F7),
                colorFondoTarjeta = colorTarjeta,
                colorBorde = colorBorde,
                onClick = onNavigateToReporte
            )

            TarjetaMenu(
                icono = "🔦",
                titulo = "Instrumental de Pista (Recursos Nativos)",
                subtitulo = "Linterna baliza, GPS en vivo y brújula de cabezal",
                colorAcento = Color(0xFF10B981),
                colorFondoTarjeta = colorTarjeta,
                colorBorde = colorBorde,
                onClick = onNavigateToRecursos
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Pista bloqueada con PIN", Toast.LENGTH_SHORT).show()
                    onLogout()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorCeleste.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorCeleste),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Bloquear Pista (Salir a PIN)", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                    onCerrarSesionCompleta()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Cerrar Sesión (Cambiar Cuenta)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TarjetaMenu(
    icono: String,
    titulo: String,
    subtitulo: String,
    colorAcento: Color,
    colorFondoTarjeta: Color = Color(0xFF13223E),
    colorBorde: Color = Color(0xFF1E3A5F),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondoTarjeta),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorAcento.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icono, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = "›",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorAcento
            )
        }
    }
}
