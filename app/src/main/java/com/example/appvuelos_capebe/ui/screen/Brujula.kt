package com.example.appvuelos_capebe.ui.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun rememberGradosBrujula(): Float {
    val context = LocalContext.current
    var gradosSuavizados by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        @Suppress("DEPRECATION")
        val sensorOrientacion = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val nuevo = (it.values[0] + 360f) % 360f
                    var diff = nuevo - gradosSuavizados
                    if (diff > 180f) diff -= 360f
                    if (diff < -180f) diff += 360f

                    if (abs(diff) > 1.5f) {
                        gradosSuavizados = (gradosSuavizados + diff * 0.15f + 360f) % 360f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorOrientacion != null) {
            sensorManager.registerListener(
                listener,
                sensorOrientacion,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val gradosAnimados by animateFloatAsState(
        targetValue = gradosSuavizados,
        animationSpec = tween(durationMillis = 250),
        label = "brujula"
    )

    return gradosAnimados
}

@Composable
fun BrujulaRedonda(
    grados: Float,
    tamano: Dp = 140.dp,
    esMini: Boolean = false
) {
    val fuenteCardinal = if (esMini) 8.sp else 14.sp
    val fuenteAguja = if (esMini) 12.sp else 26.sp

    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(Color(0xFF0A192F))
            .border(2.dp, Color(0xFF38BDF8), CircleShape)
            .rotate(-grados)
            .padding(if (esMini) 2.dp else 8.dp)
    ) {
        Text(
            text = "N",
            color = Color(0xFFEF4444),
            fontSize = fuenteCardinal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "S",
            color = Color.White,
            fontSize = fuenteCardinal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Text(
            text = "O",
            color = Color.White,
            fontSize = fuenteCardinal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "E",
            color = Color.White,
            fontSize = fuenteCardinal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Text(
            text = "▲",
            color = Color(0xFFEF4444),
            fontSize = fuenteAguja,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun MiniBrujulaHeader(modifier: Modifier = Modifier) {
    val grados = rememberGradosBrujula()
    Box(modifier = modifier) {
        BrujulaRedonda(grados = grados, tamano = 44.dp, esMini = true)
    }
}

@Composable
fun TarjetaBrujula() {
    val grados = rememberGradosBrujula()
    val gradosInt = grados.roundToInt() % 360

    val direccion = when (gradosInt) {
        in 45..135 -> "Este (E)"
        in 136..225 -> "Sur (S)"
        in 226..315 -> "Oeste (O)"
        else -> "Norte (N)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Brújula de Cabezal de Pista",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            BrujulaRedonda(grados = grados, tamano = 140.dp, esMini = false)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$gradosInt° $direccion",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            Text(
                text = "Rumbo magnético en losa",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
