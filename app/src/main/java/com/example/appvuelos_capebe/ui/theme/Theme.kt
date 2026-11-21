package com.example.appvuelos_capebe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CelesteAeronautico,
    onPrimary = AzulOscuroFondo,
    secondary = AmbarAlerta,
    background = AzulOscuroFondo,
    surface = AzulTarjeta,
    onBackground = TextoBlanco,
    onSurface = TextoBlanco
)

@Composable
fun AppVuelos_CAPEBETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
