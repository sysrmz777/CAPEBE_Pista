package com.example.appvuelos_capebe.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val SCTB_LAT = -33.4552
private const val SCTB_LON = -70.5476
private const val SCIE_LAT = -36.7726
private const val SCIE_LON = -73.0631

@SuppressLint("MissingPermission", "ClickableViewAccessibility")
@Composable
fun TarjetaGps() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var tienePermiso by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    var gpsHabilitado by remember { mutableStateOf(estaUbicacionActiva(context)) }

    var ubicacionReal by remember { mutableStateOf<Location?>(null) }
    var horaActualizacion by remember { mutableStateOf("--:--:--") }
    var focoMapa by remember { mutableStateOf("GPS") } // "GPS", "SCTB", "SCIE"
    var recargaToken by remember { mutableIntStateOf(0) }
    var centroInicialHecho by remember { mutableStateOf(false) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var userMarkerRef by remember { mutableStateOf<Marker?>(null) }

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        tienePermiso = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        gpsHabilitado = estaUbicacionActiva(context)
        if (tienePermiso) recargaToken++
    }

    // Ciclo de vida del MapView nativo y re-verificación de GPS al volver de Ajustes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapViewRef?.onResume()
                    tienePermiso = tienePermisoUbicacion(context)
                    gpsHabilitado = estaUbicacionActiva(context)
                    if (tienePermiso && gpsHabilitado) {
                        recargaToken++
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapViewRef?.onPause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) {
            launcherPermisos.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Motor GPS real: FusedLocationProviderClient + respaldo LocationManager (GPS + Red)
    DisposableEffect(tienePermiso, gpsHabilitado, recargaToken) {
        if (!tienePermiso || !gpsHabilitado) {
            onDispose { }
        } else {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            fun aplicarUbicacion(loc: Location) {
                ubicacionReal = loc
                horaActualizacion = formatoHora.format(Date(if (loc.time > 0L) loc.time else System.currentTimeMillis()))
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { aplicarUbicacion(it) }
                }
            }

            var fallbackManager: LocationManager? = null
            var fallbackListener: LocationListener? = null

            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) aplicarUbicacion(loc)
                }

                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) aplicarUbicacion(loc)
                    }

                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2500L)
                    .setMinUpdateIntervalMillis(1000L)
                    .setMinUpdateDistanceMeters(1f)
                    .build()

                fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            } catch (_: Exception) {
                try {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) = aplicarUbicacion(location)
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                    fallbackManager = lm
                    fallbackListener = listener

                    val ultima = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (ultima != null) aplicarUbicacion(ultima)

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, listener)
                    }
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1f, listener)
                    }
                } catch (_: Exception) {}
            }

            onDispose {
                cts.cancel()
                try {
                    fusedClient.removeLocationUpdates(locationCallback)
                } catch (_: Exception) {}
                try {
                    if (fallbackManager != null && fallbackListener != null) {
                        fallbackManager.removeUpdates(fallbackListener)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Actualizar marcador GPS y cámara en el MapView nativo (osmdroid)
    LaunchedEffect(ubicacionReal, focoMapa, mapViewRef) {
        val mv = mapViewRef ?: return@LaunchedEffect
        val loc = ubicacionReal

        if (loc != null) {
            val puntoGps = GeoPoint(loc.latitude, loc.longitude)
            val marker = userMarkerRef ?: Marker(mv).apply {
                title = "Tu ubicación GPS"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = crearIconoPunto(context, AndroidColor.parseColor("#0EA5E9"), 20)
                mv.overlays.add(this)
                userMarkerRef = this
            }
            marker.position = puntoGps
            marker.snippet = "Precisión ±${loc.accuracy.roundToInt()} m"

            if (focoMapa == "GPS" || !centroInicialHecho) {
                centroInicialHecho = true
                mv.controller.setZoom(15.5)
                mv.controller.animateTo(puntoGps)
            }
        }

        when (focoMapa) {
            "SCTB" -> {
                mv.controller.setZoom(15.0)
                mv.controller.animateTo(GeoPoint(SCTB_LAT, SCTB_LON))
            }
            "SCIE" -> {
                mv.controller.setZoom(14.5)
                mv.controller.animateTo(GeoPoint(SCIE_LAT, SCIE_LON))
            }
        }
        mv.invalidate()
    }

    val locActual = ubicacionReal
    val latMostrada = when {
        focoMapa == "SCTB" -> SCTB_LAT
        focoMapa == "SCIE" -> SCIE_LAT
        locActual != null -> locActual.latitude
        else -> SCTB_LAT
    }
    val lonMostrada = when {
        focoMapa == "SCTB" -> SCTB_LON
        focoMapa == "SCIE" -> SCIE_LON
        locActual != null -> locActual.longitude
        else -> SCTB_LON
    }

    val estadoTexto = when {
        !tienePermiso -> "SIN PERMISO"
        !gpsHabilitado -> "GPS APAGADO"
        locActual != null -> "GPS EN VIVO (±${locActual.accuracy.roundToInt()}m)"
        else -> "BUSCANDO SEÑAL..."
    }
    val estadoColor = when {
        !tienePermiso -> Color(0xFFEF4444)
        !gpsHabilitado -> Color(0xFFF59E0B)
        locActual != null -> Color(0xFF10B981)
        else -> Color(0xFF38BDF8)
    }

    val distTobalaba = locActual?.let { calcularDistanciaYRumbo(it, SCTB_LAT, SCTB_LON) }
    val distTalcahuano = locActual?.let { calcularDistanciaYRumbo(it, SCIE_LAT, SCIE_LON) }

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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS y Mapa de Losa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(estadoColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(estadoColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = estadoTexto,
                        color = estadoColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (!tienePermiso) {
                Button(
                    onClick = {
                        launcherPermisos.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permitir Acceso al GPS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else if (!gpsHabilitado) {
                Button(
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Activa la Ubicación en Ajustes", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF070D19)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activar GPS en Ajustes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Mapa nativo compacto (osmdroid + Carto Voyager, 0 WebView, carga rápido en celulares)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A192F))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val config = Configuration.getInstance()
                        config.userAgentValue = "CAPEBE-Android/1.0 (${ctx.packageName})"
                        config.osmdroidBasePath = File(ctx.cacheDir, "osmdroid_osm")
                        config.osmdroidTileCache = File(ctx.cacheDir, "osmdroid_osm/tiles")

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                            controller.setZoom(14.5)
                            controller.setCenter(GeoPoint(SCTB_LAT, SCTB_LON))

                            // Marcadores permanentes de las bases CAPEBE
                            val markerSctb = Marker(this).apply {
                                position = GeoPoint(SCTB_LAT, SCTB_LON)
                                title = "Aeródromo Tobalaba (SCTB)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                icon = crearIconoPunto(ctx, AndroidColor.parseColor("#38BDF8"), 16)
                            }
                            val markerScie = Marker(this).apply {
                                position = GeoPoint(SCIE_LAT, SCIE_LON)
                                title = "Carriel Sur Talcahuano (SCIE)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                icon = crearIconoPunto(ctx, AndroidColor.parseColor("#F59E0B"), 16)
                            }
                            overlays.add(markerSctb)
                            overlays.add(markerScie)

                            setOnTouchListener { v, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                                false
                            }

                            onResume()
                            mapViewRef = this
                        }
                    },
                    onRelease = { mv ->
                        mv.onPause()
                        mv.onDetach()
                    }
                )
            }

            // Telemetría GPS compacta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A192F))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val tituloFoco = when (focoMapa) {
                    "SCTB" -> "Base Tobalaba (SCTB)"
                    "SCIE" -> "Base Talcahuano (SCIE)"
                    else -> if (locActual != null) "Tu Posición GPS ($horaActualizacion)" else "Esperando señal GPS..."
                }
                Text(
                    text = tituloFoco,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LATITUD", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        Text(
                            text = String.format(Locale.US, "%.6f°", latMostrada),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LONGITUD", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        Text(
                            text = String.format(Locale.US, "%.6f°", lonMostrada),
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (locActual != null) {
                    val velKmh = (locActual.speed * 3.6f).coerceAtLeast(0f)
                    val velKt = (locActual.speed * 1.94384f).coerceAtLeast(0f)
                    val altitudTxt = if (locActual.hasAltitude()) "${locActual.altitude.roundToInt()} m" else "N/D"

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ALTITUD / PRECISIÓN", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            Text(
                                text = "$altitudTxt • ±${locActual.accuracy.roundToInt()} m",
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("VELOCIDAD", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            Text(
                                text = String.format(Locale.US, "%.1f km/h (%.0f kt)", velKmh, velKt),
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "✈ SCTB: $distTobalaba  |  SCIE: $distTalcahuano",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botones compactos de ubicación y bases
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        focoMapa = "GPS"
                        gpsHabilitado = estaUbicacionActiva(context)
                        recargaToken++
                        locActual?.let {
                            mapViewRef?.controller?.setZoom(15.5)
                            mapViewRef?.controller?.animateTo(GeoPoint(it.latitude, it.longitude))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📍 Mi Ubic.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { focoMapa = "SCTB" },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✈ Tobalaba", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { focoMapa = "SCIE" },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✈ Talcahuano", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
            }

            // Botón funcional directo a Google Maps
            Button(
                onClick = {
                    val etiqueta = when (focoMapa) {
                        "SCTB" -> "Aerodromo Tobalaba SCTB"
                        "SCIE" -> "Aeropuerto Carriel Sur Talcahuano SCIE"
                        else -> "Mi Ubicacion GPS CAPEBE"
                    }
                    abrirEnGoogleMaps(context, latMostrada, lonMostrada, etiqueta)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF070D19)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Text(
                    text = "🗺 Abrir en Google Maps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun crearIconoPunto(context: Context, colorHex: Int, sizeDp: Int): GradientDrawable {
    val density = context.resources.displayMetrics.density
    val px = (sizeDp * density).roundToInt()
    val strokePx = (2.5f * density).roundToInt().coerceAtLeast(2)
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setSize(px, px)
        setColor(colorHex)
        setStroke(strokePx, AndroidColor.WHITE)
    }
}

private fun tienePermisoUbicacion(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun estaUbicacionActiva(context: Context): Boolean {
    return try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    } catch (_: Exception) {
        true
    }
}

private fun calcularDistanciaYRumbo(origen: Location, destLat: Double, destLon: Double): String {
    val destino = Location("base").apply {
        latitude = destLat
        longitude = destLon
    }
    val metros = origen.distanceTo(destino)
    val rumboRaw = origen.bearingTo(destino)
    val rumbo = ((rumboRaw + 360f) % 360f).roundToInt()
    val cardinales = arrayOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    val cardinal = cardinales[((rumbo + 22.5) / 45.0).toInt() % 8]
    val distTxt = if (metros < 1000f) {
        "${metros.roundToInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", metros / 1000f)
    }
    return "$distTxt (${rumbo}° $cardinal)"
}

private fun abrirEnGoogleMaps(context: Context, lat: Double, lon: Double, label: String) {
    val latStr = String.format(Locale.US, "%.6f", lat)
    val lonStr = String.format(Locale.US, "%.6f", lon)
    val mapsAppUri = Uri.parse("geo:0,0?q=$latStr,$lonStr(${Uri.encode(label)})")

    // 1. Intentar abrir directamente en la app de Google Maps con pin y ruta
    try {
        val intentMaps = Intent(Intent.ACTION_VIEW, mapsAppUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intentMaps)
        return
    } catch (_: Exception) {}

    // 2. Respaldo: cualquier app de mapas instalada
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsAppUri))
        return
    } catch (_: Exception) {}

    // 3. Respaldo garantizado: abrir Google Maps en el navegador
    try {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latStr,$lonStr")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show()
    }
}
