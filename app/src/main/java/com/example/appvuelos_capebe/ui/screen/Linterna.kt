package com.example.appvuelos_capebe.ui.screen

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TarjetaLinterna(context: Context) {
    var encendida by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🔦 Baliza / Linterna de Losa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = if (encendida) "Estado: ENCENDIDA (Señalización activa)" else "Estado: APAGADA",
                    fontSize = 12.sp,
                    color = if (encendida) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                )
            }
            Switch(
                checked = encendida,
                onCheckedChange = { nuevoEstado ->
                    if (cambiarLinterna(context, nuevoEstado)) {
                        encendida = nuevoEstado
                        vibrar(context, 120L)
                    } else {
                        Toast.makeText(
                            context,
                            "Flash no disponible en este dispositivo/emulador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFF59E0B),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF0A192F)
                )
            )
        }
    }
}

fun cambiarLinterna(context: Context, encender: Boolean): Boolean {
    return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val idCamara = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return false

        cameraManager.setTorchMode(idCamara, encender)
        Log.d("Linterna", "Luz en cámara $idCamara -> ${if (encender) "ENCENDIDA" else "APAGADA"}")
        true
    } catch (e: Exception) {
        Log.w("Linterna", "No se pudo cambiar linterna: ${e.localizedMessage}")
        false
    }
}
