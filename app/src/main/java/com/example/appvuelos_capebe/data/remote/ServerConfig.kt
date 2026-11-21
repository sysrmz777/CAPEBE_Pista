package com.example.appvuelos_capebe.data.remote

object ServerConfig {
    // Emulador Android: "10.0.2.2"
    // Celular físico en el mismo Wi-Fi: pon la IP local de tu PC (ej: "192.168.1.50")
    // Celular físico por cable USB (adb reverse): "127.0.0.1"
    // Producción en AWS EC2: pon tu IP pública de AWS
    const val HOST = "34.229.165.5"

    fun url(puerto: Int): String = "http://$HOST:$puerto/"
}