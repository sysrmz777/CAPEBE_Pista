package com.example.appvuelos_capebe.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generarBitmapQrPista(contenido: String, pixeles: Int = 380): Bitmap? {
    return try {
        val matrix = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, pixeles, pixeles)
        val bmp = Bitmap.createBitmap(pixeles, pixeles, Bitmap.Config.ARGB_8888)
        for (x in 0 until pixeles) {
            for (y in 0 until pixeles) {
                bmp.setPixel(x, y, if (matrix[x, y]) AndroidColor.rgb(10, 25, 47) else AndroidColor.WHITE)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

@Composable
fun CodigoQrVista(
    codigo: String,
    tamano: Dp = 140.dp,
    colorTexto: Color = Color(0xFF38BDF8)
) {
    val safeCodigo = codigo.ifBlank { "CAPEBE-SCTB-1001" }
    val bitmap = remember(safeCodigo) { generarBitmapQrPista(safeCodigo) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(tamano)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Código QR $safeCodigo",
                    modifier = Modifier.size(tamano - 16.dp)
                )
            } else {
                Text(text = safeCodigo, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Código QR: $safeCodigo",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
    }
}
