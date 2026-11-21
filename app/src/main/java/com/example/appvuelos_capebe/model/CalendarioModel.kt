package com.example.appvuelos_capebe.model

import com.example.appvuelos_capebe.data.remote.AeronaveDto
import com.example.appvuelos_capebe.data.remote.ReporteDiarioDto
import java.time.LocalDate

data class CalendarioModel(
    val fechaFiltro: LocalDate? = null,
    val vuelos: List<VueloModel> = emptyList(),
    val vuelosGuardados: List<VueloModel> = emptyList(),
    val aeronaves: List<AeronaveDto> = emptyList(),
    val vueloElegido: VueloModel? = null,
    // Validación real de QR / código único
    val codigoIngresado: String = "",
    val qrLeido: Boolean = false,
    val qrVerificadoRemoto: Boolean = false,
    val mensajeQr: String? = null,
    // Checklist + Horómetro riguroso DGAC (XXXX.X)
    val pistaDespejada: Boolean = true,
    val combustibleOk: Boolean = true,
    val lucesOk: Boolean = true,
    val pilotoAlMando: String = "Cap. Rodrigo Fuentes",
    val horometroInicialTexto: String = "1245.2",
    val horometroFinalTexto: String = "1245.5",
    val observaciones: String = "",
    // Reporte Diario DGAC / Mantenimiento
    val reporteDiario: ReporteDiarioDto? = null,
    // Estado de red
    val cargando: Boolean = false,
    val conectadoPista: Boolean = false,
    val conectadoQr: Boolean = false,
    val mensajeServidor: String? = null,
    val mensajeError: String? = null
)
