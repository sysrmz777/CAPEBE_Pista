package com.example.appvuelos_capebe.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class VueloSocioDto(
    val id: Long? = null,
    val pasajero: String? = null,
    val rut: String? = null,
    val oficinaBanco: String? = null,
    val esSocio: Boolean? = true,
    val telefonoMovil: String? = null,
    val cantidadPasajeros: Int? = 1,
    val fecha: String? = null,
    val bloqueHorario: String? = null,
    val aeronave: String? = null,
    val plazasAeronave: Int? = 4,
    val baseAerea: String? = null,
    val destino: String? = null,
    val pilotoAlMando: String? = null,
    val codigoVerificacion: String? = null
)

interface VuelosSocioApi {
    @GET("api/vuelos")
    suspend fun listarVuelosSocio(): List<VueloSocioDto>

    @GET("api/vuelos/codigo/{codigo}")
    suspend fun buscarPorCodigo(@Path("codigo") codigo: String): VueloSocioDto
}

object RetrofitPista {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var cachedHost: String = ""
    private var cachedVuelosApi: VuelosSocioApi? = null
    private var cachedPistaApi: PistaApi? = null
    private var cachedQrApi: QrApi? = null

    private fun asegurarClientes() {
        val host = ServerConfig.HOST
        if (host != cachedHost || cachedPistaApi == null) {
            cachedHost = host
            cachedVuelosApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8082))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VuelosSocioApi::class.java)

            cachedPistaApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8083))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PistaApi::class.java)

            cachedQrApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8084))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QrApi::class.java)
        }
    }

    val vuelosSocioApi: VuelosSocioApi
        get() {
            asegurarClientes()
            return cachedVuelosApi!!
        }

    val pistaApi: PistaApi
        get() {
            asegurarClientes()
            return cachedPistaApi!!
        }

    val qrApi: QrApi
        get() {
            asegurarClientes()
            return cachedQrApi!!
        }
}
