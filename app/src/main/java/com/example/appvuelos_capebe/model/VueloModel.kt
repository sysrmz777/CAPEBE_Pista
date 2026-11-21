package com.example.appvuelos_capebe.model

data class VueloModel(
    val idOperacion: Long? = null,
    val fecha: String = "",
    val horario: String = "",
    val horaLocal: String = "",
    val base: String = "",
    val destino: String = "",
    val aeronave: String = "",
    val matricula: String = "",
    val plazasAeronave: Int = 4,
    val pilotoAlMando: String = "Cap. Rodrigo Fuentes",
    // Datos identificatorios del invitado/socio
    val pasajero: String = "Socio CAPEBE",
    val rut: String = "",
    val oficinaBanco: String = "",
    val esSocio: Boolean = true,
    val telefonoMovil: String = "",
    val cantidadPasajeros: Int = 1,
    // Control QR y horómetro DGAC
    val codigoQr: String = "CAPEBE-SCTB-1001",
    val estadoPista: String = "PENDIENTE EN LOSA",
    val encargadoPista: String = "",
    val observaciones: String = "",
    val pistaDespejada: Boolean = true,
    val combustibleOk: Boolean = true,
    val lucesOk: Boolean = true,
    val horometroInicial: Double? = null,
    val horometroFinal: Double? = null,
    val duracionMinutos: Int? = null
)
