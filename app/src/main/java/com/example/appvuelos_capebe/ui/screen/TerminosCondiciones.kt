package com.example.appvuelos_capebe.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appvuelos_capebe.ui.components.CodigoQrVista
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun TerminosCondiciones(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    viewModel: CalendarioViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val vuelo = state.vueloElegido

    // Escáner real de Código QR con Cámara (ZXing Embedded en modo vertical)
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val contenido = result.contents?.trim()
        if (!contenido.isNullOrBlank()) {
            vibrar(context, 160L)
            Toast.makeText(context, "QR escaneado: $contenido", Toast.LENGTH_SHORT).show()
            viewModel.procesarQrEscaneado(contenido)
        }
    }

    fun abrirEscanerQr() {
        val opciones = ScanOptions().apply {
            setCaptureActivity(QrScannerActivity::class.java)
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Apunta la cámara al código QR del Socio CAPEBE")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(true)
        }
        qrScannerLauncher.launch(opciones)
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirEscanerQr()
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso de Cámara para escanear el QR del socio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = Color(0xFF1E3A5F),
        focusedLabelColor = Color(0xFF38BDF8),
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedPlaceholderColor = Color(0xFF64748B),
        unfocusedPlaceholderColor = Color(0xFF64748B),
        cursorColor = Color(0xFF38BDF8)
    )

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = Color(0xFF0284C7),
        uncheckedThumbColor = Color(0xFF94A3B8),
        uncheckedTrackColor = Color(0xFF0A192F)
    )

    Scaffold(
        topBar = { Header("EMBARQUE QR Y HORÓMETRO DGAC") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1. Lectura y Validación de Código QR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                if (state.codigoIngresado.isNotBlank() || vuelo != null) {
                    OutlinedButton(
                        onClick = { viewModel.limpiarFormularioOperacion() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF64748B))
                    ) {
                        Text("Limpiar", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón principal: Escáner REAL con cámara
                    Button(
                        onClick = {
                            vibrar(context, 120L)
                            val tieneCamara = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (tieneCamara) {
                                abrirEscanerQr()
                            } else {
                                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        enabled = !state.cargando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color(0xFF070D19)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "📷 Escanear Código QR con Cámara",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "O ingresa el código único manualmente:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = state.codigoIngresado,
                        onValueChange = { viewModel.onCodigoIngresadoChanged(it) },
                        label = { Text("Código QR / Verificación Único") },
                        placeholder = { Text("Ej: CAPEBE-SCTB-1001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Button(
                        onClick = {
                            vibrar(context, 180L)
                            viewModel.leerYValidarCodigoQr()
                        },
                        enabled = !state.cargando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.cargando) "Consultando PostgreSQL..." else "Validar Código en Servidor",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    state.mensajeQr?.let { msg ->
                        Text(text = msg, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (vuelo != null) {
                        CodigoQrVista(codigo = vuelo.codigoQr, tamano = 120.dp)

                        // Ficha identificatoria del pasajero para control de embarque en terreno
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A192F), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "IDENTIFICACIÓN DE PASAJERO EN TERRENO",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Nombres y Apellidos: ${vuelo.pasajero}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("RUT: ${vuelo.rut.ifBlank { "No informado" }}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("Oficina BancoEstado: ${vuelo.oficinaBanco.ifBlank { "No informada" }}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text(
                                text = "Calidad de Socio: ${if (vuelo.esSocio) "SÍ — SOCIO TITULAR" else "NO — INVITADO"}",
                                color = if (vuelo.esSocio) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text("Teléfono Móvil: ${vuelo.telefonoMovil.ifBlank { "No informado" }}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Fecha / Bloque: ${vuelo.fecha} • ${vuelo.horario}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Pasajeros: ${vuelo.cantidadPasajeros} / ${vuelo.plazasAeronave - 1} asientos (${vuelo.aeronave})", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = "2. Checklist Normativo de Losa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "1. Pista y losa despejadas", color = Color.White)
                        Switch(checked = state.pistaDespejada, onCheckedChange = { viewModel.onPistaDespejadaChanged(it) }, colors = switchColors)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "2. Combustible 100LL verificado", color = Color.White)
                        Switch(checked = state.combustibleOk, onCheckedChange = { viewModel.onCombustibleOkChanged(it) }, colors = switchColors)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "3. Luces y balizas operativas", color = Color.White)
                        Switch(checked = state.lucesOk, onCheckedChange = { viewModel.onLucesOkChanged(it) }, colors = switchColors)
                    }
                }
            }

            Text(
                text = "3. Registro Riguroso de Horómetro (DGAC)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Formato obligatorio: 4 dígitos + 1 decimal (ej: 1245.2 → 1245.5). Siempre en aumento y vuelo ≤ 20 min.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = state.pilotoAlMando,
                        onValueChange = { viewModel.onPilotoAlMandoChanged(it) },
                        label = { Text("Piloto al Mando") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.horometroInicialTexto,
                            onValueChange = { viewModel.onHorometroInicialChanged(it) },
                            label = { Text("Horóm. Inicial") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = state.horometroFinalTexto,
                            onValueChange = { viewModel.onHorometroFinalChanged(it) },
                            label = { Text("Horóm. Final") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = state.observaciones,
                        onValueChange = { viewModel.onObservacionesChanged(it) },
                        label = { Text("Observaciones de Pista") },
                        placeholder = { Text("Ej: Identidad verificada, calzos retirados") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }
            }

            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = Color(0xFFF87171),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.limpiarFormularioOperacion()
                        onDecline()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (viewModel.validarCondicionesPista()) {
                            vibrar(context, 350L)
                            onAccept()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Aprobar Pista", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
