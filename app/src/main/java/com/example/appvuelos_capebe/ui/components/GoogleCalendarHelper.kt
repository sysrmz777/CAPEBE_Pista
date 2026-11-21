package com.example.appvuelos_capebe.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object GoogleCalendarHelper {

    fun agregarVueloAGoogleCalendar(
        context: Context,
        fechaIso: String,
        bloqueHorario: String,
        aeronave: String,
        baseAerea: String,
        pasajero: String,
        codigoVerificacion: String,
        detalleExtra: String = ""
    ) {
        val fecha = runCatching { LocalDate.parse(fechaIso.trim()) }.getOrElse { LocalDate.now() }
        val partesHora = bloqueHorario.split("-")
        val horaInicio = parsearHora(partesHora.getOrNull(0)?.trim() ?: "10:00")
        val horaFin = parsearHora(partesHora.getOrNull(1)?.trim() ?: horaInicio.plusHours(1).toString())

        val zone = ZoneId.systemDefault()
        val inicioMillis = fecha.atTime(horaInicio).atZone(zone).toInstant().toEpochMilli()
        val finMillis = fecha.atTime(horaFin).atZone(zone).toInstant().toEpochMilli()

        val titulo = "Control Pista CAPEBE • $aeronave ($baseAerea)"
        val ubicacion = if (baseAerea.contains("Talcahuano", ignoreCase = true)) {
            "Aeródromo Carriel Sur (SCIE), Talcahuano, Chile"
        } else {
            "Aeródromo Eulogio Sánchez Tobalaba (SCTB), La Reina, Santiago"
        }
        val descripcion = buildString {
            appendLine("Control Operacional CAPEBE - Bloque 1h (vuelo ≤ 20 min)")
            appendLine("Código QR / Verificación: $codigoVerificacion")
            appendLine("Pasajero titular: $pasajero")
            appendLine("Aeronave: $aeronave | Base: $baseAerea")
            if (detalleExtra.isNotBlank()) appendLine(detalleExtra)
        }

        val intentCalendario = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intentCalendario)
            return
        } catch (_: Exception) {}

        try {
            val fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
            val inicioStr = fecha.atTime(horaInicio).format(fmt)
            val finStr = fecha.atTime(horaFin).format(fmt)
            val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
            val url = "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                    "&text=${enc(titulo)}" +
                    "&dates=$inicioStr/$finStr" +
                    "&details=${enc(descripcion)}" +
                    "&location=${enc(ubicacion)}"

            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se encontró aplicación de calendario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsearHora(texto: String): LocalTime {
        return runCatching {
            LocalTime.parse(texto.take(5), DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrElse { LocalTime.of(10, 0) }
    }
}
