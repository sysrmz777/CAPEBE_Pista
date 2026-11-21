package com.example.appvuelos_capebe.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class AeronaveDto(
    val idAeronave: Long? = null,
    val matricula: String,
    val modelo: String,
    val plazasTotales: Int,
    val asientosDisponibles: Int,
    val baseAerea: String,
    val horometroActual: Double,
    val horometroMaxMantenimiento: Double,
    val enMantenimiento: Boolean
)

data class OperacionPistaDto(
    val idOperacion: Long? = null,
    val fecha: String,
    val horario: String,
    val horaLocal: String? = null,
    val base: String,
    val destino: String? = null,
    val aeronave: String,
    val matricula: String? = null,
    val pilotoAlMando: String? = null,
    val pasajero: String? = null,
    val cantidadPasajeros: Int? = 1,
    val codigoQr: String? = null,
    val horometroInicial: Double? = null,
    val horometroFinal: Double? = null,
    val duracionMinutos: Int? = null,
    val estadoPista: String = "PENDIENTE EN LOSA"
)

data class ReporteDiarioDto(
    val fechaReporte: String = "",
    val destinatarios: List<String> = emptyList(),
    val totalVuelosRealizados: Int = 0,
    val totalHorasVoladas: Double = 0.0,
    val notaLegalDgac: String = "",
    val vuelosRealizados: List<OperacionPistaDto> = emptyList(),
    val estadoFlotaMantenimiento: List<AeronaveDto> = emptyList()
)

interface PistaApi {

    @GET("api/v1/pista")
    suspend fun listarOperaciones(): List<OperacionPistaDto>

    @GET("api/v1/pista/aeronaves")
    suspend fun listarAeronaves(): List<AeronaveDto>

    @GET("api/v1/pista/reporte")
    suspend fun obtenerReporteDiario(@Query("fecha") fecha: String? = null): ReporteDiarioDto

    @POST("api/v1/pista")
    suspend fun crearOperacion(@Body operacion: OperacionPistaDto): OperacionPistaDto

    @PUT("api/v1/pista/{id}/habilitar")
    suspend fun habilitarOperacion(@Path("id") id: Long): OperacionPistaDto

    @PUT("api/v1/pista/{id}/registrar-vuelo")
    suspend fun registrarVueloYHorometro(
        @Path("id") id: Long,
        @Body datosVuelo: OperacionPistaDto
    ): OperacionPistaDto
}
