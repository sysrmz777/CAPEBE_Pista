package com.example.appvuelos_capebe.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class PaseQrDto(
    val idQr: Long? = null,
    val codigoQr: String,
    val socio: String = "Socio CAPEBE",
    val rut: String? = null,
    val oficinaBanco: String? = null,
    val esSocio: Boolean? = true,
    val telefonoMovil: String? = null,
    val cantidadPasajeros: Int? = 1,
    val plazasAeronave: Int? = 4,
    val fecha: String,
    val horario: String,
    val base: String,
    val aeronave: String,
    val destino: String? = null,
    val pilotoAlMando: String? = null,
    val encargadoPista: String? = null,
    val observaciones: String? = null,
    val pistaDespejada: Boolean = true,
    val combustibleOk: Boolean = true,
    val lucesOk: Boolean = true,
    val horometroInicial: Double? = null,
    val horometroFinal: Double? = null,
    val estadoQr: String = "QR EMITIDO - PENDIENTE EN PISTA"
)

interface QrApi {

    @GET("api/v1/qr")
    suspend fun listarQrs(): List<PaseQrDto>

    @GET("api/v1/qr/codigo/{codigoQr}")
    suspend fun leerPorCodigo(@Path("codigoQr") codigoQr: String): PaseQrDto

    @POST("api/v1/qr/generar")
    suspend fun generarQr(@Body pase: PaseQrDto): PaseQrDto

    @PUT("api/v1/qr/validar/{codigoQr}")
    suspend fun validarQrEnPista(
        @Path("codigoQr") codigoQr: String,
        @Body datosValidacion: PaseQrDto
    ): PaseQrDto
}
