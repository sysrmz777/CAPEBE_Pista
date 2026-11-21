package com.example.appvuelos_capebe.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appvuelos_capebe.data.remote.AeronaveDto
import com.example.appvuelos_capebe.data.remote.OperacionPistaDto
import com.example.appvuelos_capebe.data.remote.PaseQrDto
import com.example.appvuelos_capebe.data.remote.ReporteDiarioDto
import com.example.appvuelos_capebe.data.remote.RetrofitPista
import com.example.appvuelos_capebe.model.CalendarioModel
import com.example.appvuelos_capebe.model.VueloModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

class CalendarioViewModel : ViewModel() {

    private val horometroRegex = Regex("^\\d{4}\\.\\d$")

    var uiState by mutableStateOf(
        CalendarioModel(
            vuelos = crearVuelosRespaldo(),
            aeronaves = crearFlotaRespaldo()
        )
    )
        private set

    init {
        cargarDesdeMicroservicios()
    }

    fun cargarDesdeMicroservicios() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true, mensajeServidor = null)

            var pistaOk = false
            var qrOk = false
            var vuelosSocioOk = false
            var listaOps = emptyList<OperacionPistaDto>()
            var listaQrs = emptyList<PaseQrDto>()
            var listaVuelosSocio = emptyList<com.example.appvuelos_capebe.data.remote.VueloSocioDto>()
            var listaAviones = uiState.aeronaves

            try {
                listaAviones = RetrofitPista.pistaApi.listarAeronaves()
            } catch (_: Exception) {}

            // 1. GET /api/v1/pista (service-pista :8083)
            try {
                listaOps = RetrofitPista.pistaApi.listarOperaciones()
                pistaOk = true
            } catch (_: Exception) {}

            // 2. GET /api/v1/qr (service-qr :8084)
            try {
                listaQrs = RetrofitPista.qrApi.listarQrs()
                qrOk = true
            } catch (_: Exception) {}

            // 3. GET /api/vuelos (service-vuelos :8082) — enlace directo con reservas del Socio
            try {
                listaVuelosSocio = RetrofitPista.vuelosSocioApi.listarVuelosSocio()
                vuelosSocioOk = true
            } catch (_: Exception) {}

            val qrPorCodigo = listaQrs.associateBy { it.codigoQr.uppercase() }
            val combinados = mutableListOf<VueloModel>()
            val codigosAgregados = mutableSetOf<String>()

            // Unir operaciones de pista con datos completos del pasajero de service-qr
            listaOps.forEachIndexed { idx, op ->
                val prefijo = if (op.base.contains("Talcahuano", ignoreCase = true)) "SCIE" else "SCTB"
                val codigo = op.codigoQr?.takeIf { it.isNotBlank() } ?: "CAPEBE-$prefijo-100${idx + 1}"
                val qr = qrPorCodigo[codigo.uppercase()]
                codigosAgregados.add(codigo.uppercase())

                combinados.add(
                    VueloModel(
                        idOperacion = op.idOperacion,
                        fecha = op.fecha,
                        horario = op.horario,
                        horaLocal = op.horaLocal ?: op.horario.split("-").first().trim(),
                        base = op.base,
                        destino = op.destino ?: qr?.destino ?: "Circuito ${op.base}",
                        aeronave = op.aeronave,
                        matricula = op.matricula ?: op.aeronave.split(" ").first(),
                        plazasAeronave = qr?.plazasAeronave ?: 4,
                        pilotoAlMando = op.pilotoAlMando ?: qr?.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                        pasajero = qr?.socio ?: op.pasajero ?: "Socio CAPEBE",
                        rut = qr?.rut ?: "",
                        oficinaBanco = qr?.oficinaBanco ?: "",
                        esSocio = qr?.esSocio ?: true,
                        telefonoMovil = qr?.telefonoMovil ?: "",
                        cantidadPasajeros = qr?.cantidadPasajeros ?: op.cantidadPasajeros ?: 1,
                        codigoQr = codigo,
                        estadoPista = op.estadoPista,
                        encargadoPista = qr?.encargadoPista ?: "",
                        observaciones = qr?.observaciones ?: "",
                        horometroInicial = op.horometroInicial ?: qr?.horometroInicial,
                        horometroFinal = op.horometroFinal ?: qr?.horometroFinal,
                        duracionMinutos = op.duracionMinutos
                    )
                )
            }

            // Incluir QRs nuevos generados por la App del Socio que aún no estén en service-pista
            listaQrs.forEach { qr ->
                if (!codigosAgregados.contains(qr.codigoQr.uppercase())) {
                    codigosAgregados.add(qr.codigoQr.uppercase())
                    combinados.add(
                        VueloModel(
                            idOperacion = null,
                            fecha = qr.fecha,
                            horario = qr.horario,
                            horaLocal = qr.horario.split("-").first().trim(),
                            base = qr.base,
                            destino = qr.destino ?: "Circuito ${qr.base}",
                            aeronave = qr.aeronave,
                            matricula = qr.aeronave.split(" ").first(),
                            plazasAeronave = qr.plazasAeronave ?: 4,
                            pilotoAlMando = qr.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                            pasajero = qr.socio,
                            rut = qr.rut ?: "",
                            oficinaBanco = qr.oficinaBanco ?: "",
                            esSocio = qr.esSocio ?: true,
                            telefonoMovil = qr.telefonoMovil ?: "",
                            cantidadPasajeros = qr.cantidadPasajeros ?: 1,
                            codigoQr = qr.codigoQr,
                            estadoPista = if (qr.estadoQr.contains("VALIDADO", ignoreCase = true)) {
                                "QR VALIDADO - HABILITADO EN PISTA"
                            } else "PENDIENTE EN LOSA",
                            encargadoPista = qr.encargadoPista ?: "",
                            observaciones = qr.observaciones ?: "",
                            horometroInicial = qr.horometroInicial,
                            horometroFinal = qr.horometroFinal
                        )
                    )
                }
            }

            // Incluir también vuelos creados directamente en service-vuelos (:8082) por la App Socio
            listaVuelosSocio.forEach { vs ->
                val codigo = vs.codigoVerificacion?.takeIf { it.isNotBlank() } ?: "CAPEBE-SCTB-${vs.id ?: 9000}"
                if (!codigosAgregados.contains(codigo.uppercase())) {
                    codigosAgregados.add(codigo.uppercase())
                    val aeronaveTxt = vs.aeronave ?: "CC-AAA Cessna 172"
                    val baseTxt = vs.baseAerea ?: "Tobalaba"
                    combinados.add(
                        VueloModel(
                            idOperacion = null,
                            fecha = vs.fecha?.takeIf { it.isNotBlank() } ?: "2026-11-21",
                            horario = vs.bloqueHorario?.takeIf { it.isNotBlank() } ?: "10:00 - 11:00",
                            horaLocal = (vs.bloqueHorario ?: "10:00").split("-").first().trim(),
                            base = baseTxt,
                            destino = vs.destino ?: "Circuito $baseTxt",
                            aeronave = aeronaveTxt,
                            matricula = aeronaveTxt.split(" ").first(),
                            plazasAeronave = vs.plazasAeronave ?: 4,
                            pilotoAlMando = vs.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                            pasajero = vs.pasajero ?: "Socio CAPEBE",
                            rut = vs.rut ?: "",
                            oficinaBanco = vs.oficinaBanco ?: "",
                            esSocio = vs.esSocio ?: true,
                            telefonoMovil = vs.telefonoMovil ?: "",
                            cantidadPasajeros = vs.cantidadPasajeros ?: 1,
                            codigoQr = codigo,
                            estadoPista = "PENDIENTE EN LOSA"
                        )
                    )
                }
            }

            val validados = listaQrs.filter { it.estadoQr.contains("VALIDADO", ignoreCase = true) }.map {
                VueloModel(
                    idOperacion = it.idQr,
                    fecha = it.fecha,
                    horario = it.horario,
                    base = it.base,
                    destino = it.destino ?: "",
                    aeronave = it.aeronave,
                    matricula = it.aeronave.split(" ").first(),
                    plazasAeronave = it.plazasAeronave ?: 4,
                    pilotoAlMando = it.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                    pasajero = it.socio,
                    rut = it.rut ?: "",
                    oficinaBanco = it.oficinaBanco ?: "",
                    esSocio = it.esSocio ?: true,
                    telefonoMovil = it.telefonoMovil ?: "",
                    cantidadPasajeros = it.cantidadPasajeros ?: 1,
                    codigoQr = it.codigoQr,
                    estadoPista = it.estadoQr,
                    encargadoPista = it.encargadoPista ?: "Encargado Pista",
                    observaciones = it.observaciones ?: "",
                    pistaDespejada = it.pistaDespejada,
                    combustibleOk = it.combustibleOk,
                    lucesOk = it.lucesOk,
                    horometroInicial = it.horometroInicial,
                    horometroFinal = it.horometroFinal
                )
            }

            uiState = uiState.copy(
                vuelos = if (combinados.isNotEmpty()) combinados else uiState.vuelos,
                vuelosGuardados = if (qrOk) validados else uiState.vuelosGuardados,
                aeronaves = listaAviones,
                cargando = false,
                conectadoPista = pistaOk || vuelosSocioOk,
                conectadoQr = qrOk,
                mensajeServidor = if (!pistaOk && !qrOk && !vuelosSocioOk) {
                    "Servidor no alcanzado (Modo local activo)"
                } else null
            )
        }
    }

    fun onFechaFiltroChanged(fecha: LocalDate?) {
        uiState = uiState.copy(fechaFiltro = fecha)
    }

    fun limpiarFormularioOperacion() {
        uiState = uiState.copy(
            vueloElegido = null,
            codigoIngresado = "",
            qrLeido = false,
            qrVerificadoRemoto = false,
            mensajeQr = null,
            pistaDespejada = true,
            combustibleOk = true,
            lucesOk = true,
            pilotoAlMando = "",
            horometroInicialTexto = "",
            horometroFinalTexto = "",
            observaciones = "",
            mensajeError = null
        )
    }

    fun onVueloElegido(vuelo: VueloModel) {
        val avion = uiState.aeronaves.find { it.matricula.equals(vuelo.matricula, ignoreCase = true) }
        val iniSugerido = maxOf(
            vuelo.horometroInicial ?: 0.0,
            avion?.horometroActual ?: 1245.2
        )
        val finSugerido = ((iniSugerido + 0.3) * 10.0).roundToInt() / 10.0

        uiState = uiState.copy(
            vueloElegido = vuelo,
            codigoIngresado = vuelo.codigoQr,
            qrLeido = false,
            qrVerificadoRemoto = false,
            mensajeQr = null,
            pistaDespejada = true,
            combustibleOk = true,
            lucesOk = true,
            pilotoAlMando = vuelo.pilotoAlMando.ifBlank { "Cap. Rodrigo Fuentes" },
            horometroInicialTexto = String.format(Locale.US, "%.1f", iniSugerido),
            horometroFinalTexto = String.format(Locale.US, "%.1f", finSugerido),
            observaciones = "",
            mensajeError = null
        )
    }

    fun onCodigoIngresadoChanged(texto: String) {
        val limpio = texto.uppercase().trim()
        val mismoVuelo = uiState.vueloElegido?.codigoQr?.equals(limpio, ignoreCase = true) == true
        uiState = uiState.copy(
            codigoIngresado = limpio,
            vueloElegido = if (mismoVuelo) uiState.vueloElegido else null,
            qrLeido = false,
            qrVerificadoRemoto = false,
            mensajeQr = null,
            observaciones = if (mismoVuelo) uiState.observaciones else "",
            mensajeError = null
        )
    }

    fun procesarQrEscaneado(contenidoQr: String) {
        val limpio = contenidoQr.trim().uppercase()
        if (limpio.isBlank()) {
            uiState = uiState.copy(mensajeError = "El código QR escaneado está vacío.")
            return
        }
        // Limpiar cualquier dato residual de un vuelo anterior antes de consultar el nuevo QR
        limpiarFormularioOperacion()
        uiState = uiState.copy(codigoIngresado = limpio)
        leerYValidarCodigoQr(limpio)
    }

    // Validación REAL contra service-qr (:8084) mediante código único o QR escaneado
    fun leerYValidarCodigoQr(codigoForzado: String? = null) {
        val codigo = (codigoForzado ?: uiState.codigoIngresado)
            .trim()
            .uppercase()
            .ifBlank { uiState.vueloElegido?.codigoQr?.uppercase() ?: "" }

        if (codigo.isBlank()) {
            uiState = uiState.copy(mensajeError = "Escanea el QR con cámara o ingresa el código de verificación del socio.")
            return
        }

        val operacionExistente = uiState.vuelos.find { it.codigoQr.equals(codigo, ignoreCase = true) }

        viewModelScope.launch {
            uiState = uiState.copy(
                cargando = true,
                codigoIngresado = codigo,
                qrLeido = false,
                qrVerificadoRemoto = false,
                mensajeError = null,
                mensajeQr = null
            )
            try {
                val pase = RetrofitPista.qrApi.leerPorCodigo(codigo)
                if (pase.estadoQr.contains("VALIDADO", ignoreCase = true)) {
                    uiState = uiState.copy(
                        cargando = false,
                        vueloElegido = null,
                        qrLeido = false,
                        qrVerificadoRemoto = false,
                        pilotoAlMando = "",
                        horometroInicialTexto = "",
                        horometroFinalTexto = "",
                        observaciones = "",
                        mensajeError = "Este código QR ($codigo) YA fue utilizado en pista anteriormente."
                    )
                    return@launch
                }

                val matricula = pase.aeronave.split(" ").first()
                val avion = uiState.aeronaves.find { it.matricula.equals(matricula, ignoreCase = true) }
                val iniSugerido = maxOf(
                    pase.horometroInicial ?: 0.0,
                    avion?.horometroActual ?: 1245.2
                )
                val finSugerido = ((iniSugerido + 0.3) * 10.0).roundToInt() / 10.0

                val vueloActualizado = VueloModel(
                    idOperacion = operacionExistente?.idOperacion,
                    fecha = pase.fecha,
                    horario = pase.horario,
                    horaLocal = pase.horario.split("-").first().trim(),
                    base = pase.base,
                    destino = pase.destino ?: "Circuito ${pase.base}",
                    aeronave = pase.aeronave,
                    matricula = matricula,
                    plazasAeronave = pase.plazasAeronave ?: 4,
                    pilotoAlMando = pase.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                    pasajero = pase.socio,
                    rut = pase.rut ?: "",
                    oficinaBanco = pase.oficinaBanco ?: "",
                    esSocio = pase.esSocio ?: true,
                    telefonoMovil = pase.telefonoMovil ?: "",
                    cantidadPasajeros = pase.cantidadPasajeros ?: 1,
                    codigoQr = pase.codigoQr,
                    estadoPista = "PENDIENTE EN LOSA"
                )

                uiState = uiState.copy(
                    cargando = false,
                    vueloElegido = vueloActualizado,
                    codigoIngresado = pase.codigoQr,
                    qrLeido = true,
                    qrVerificadoRemoto = true,
                    conectadoQr = true,
                    pistaDespejada = true,
                    combustibleOk = true,
                    lucesOk = true,
                    pilotoAlMando = vueloActualizado.pilotoAlMando,
                    horometroInicialTexto = String.format(Locale.US, "%.1f", iniSugerido),
                    horometroFinalTexto = String.format(Locale.US, "%.1f", finSugerido),
                    observaciones = "",
                    mensajeError = null,
                    mensajeQr = "✓ Código ${pase.codigoQr} verificado en PostgreSQL — Identidad confirmada"
                )
            } catch (_: Exception) {
                // 2. Respaldo remoto: buscar código directamente en service-vuelos (:8082) y enlazarlo
                try {
                    val vs = RetrofitPista.vuelosSocioApi.buscarPorCodigo(codigo)
                    val aeronaveTxt = vs.aeronave ?: "CC-AAA Cessna 172"
                    val baseTxt = vs.baseAerea ?: "Tobalaba"
                    val matricula = aeronaveTxt.split(" ").first()
                    val avion = uiState.aeronaves.find { it.matricula.equals(matricula, ignoreCase = true) }
                    val iniSugerido = avion?.horometroActual ?: 1245.2
                    val finSugerido = ((iniSugerido + 0.3) * 10.0).roundToInt() / 10.0

                    // Crea también el PaseQr en service-qr (:8084) para que la validación posterior persista
                    try {
                        RetrofitPista.qrApi.generarQr(
                            PaseQrDto(
                                codigoQr = codigo,
                                socio = vs.pasajero ?: "Socio CAPEBE",
                                rut = vs.rut,
                                oficinaBanco = vs.oficinaBanco,
                                esSocio = vs.esSocio ?: true,
                                telefonoMovil = vs.telefonoMovil,
                                cantidadPasajeros = vs.cantidadPasajeros ?: 1,
                                plazasAeronave = vs.plazasAeronave ?: 4,
                                fecha = vs.fecha ?: "2026-11-21",
                                horario = vs.bloqueHorario ?: "10:00 - 11:00",
                                base = baseTxt,
                                aeronave = aeronaveTxt,
                                destino = vs.destino,
                                pilotoAlMando = vs.pilotoAlMando
                            )
                        )
                    } catch (_: Exception) {}

                    val vueloEnlazado = VueloModel(
                        idOperacion = operacionExistente?.idOperacion,
                        fecha = vs.fecha ?: "2026-11-21",
                        horario = vs.bloqueHorario ?: "10:00 - 11:00",
                        horaLocal = (vs.bloqueHorario ?: "10:00").split("-").first().trim(),
                        base = baseTxt,
                        destino = vs.destino ?: "Circuito $baseTxt",
                        aeronave = aeronaveTxt,
                        matricula = matricula,
                        plazasAeronave = vs.plazasAeronave ?: 4,
                        pilotoAlMando = vs.pilotoAlMando ?: "Cap. Rodrigo Fuentes",
                        pasajero = vs.pasajero ?: "Socio CAPEBE",
                        rut = vs.rut ?: "",
                        oficinaBanco = vs.oficinaBanco ?: "",
                        esSocio = vs.esSocio ?: true,
                        telefonoMovil = vs.telefonoMovil ?: "",
                        cantidadPasajeros = vs.cantidadPasajeros ?: 1,
                        codigoQr = codigo,
                        estadoPista = "PENDIENTE EN LOSA"
                    )

                    uiState = uiState.copy(
                        cargando = false,
                        vueloElegido = vueloEnlazado,
                        codigoIngresado = codigo,
                        qrLeido = true,
                        qrVerificadoRemoto = true,
                        conectadoPista = true,
                        pistaDespejada = true,
                        combustibleOk = true,
                        lucesOk = true,
                        pilotoAlMando = vueloEnlazado.pilotoAlMando,
                        horometroInicialTexto = String.format(Locale.US, "%.1f", iniSugerido),
                        horometroFinalTexto = String.format(Locale.US, "%.1f", finSugerido),
                        observaciones = "",
                        mensajeError = null,
                        mensajeQr = "✓ Código $codigo verificado en PostgreSQL (enlazado desde App Socio)"
                    )
                    return@launch
                } catch (_: Exception) {}

                // 3. Respaldo local si el dispositivo está sin conexión al servidor
                val coincidenciaLocal = uiState.vuelos.find { it.codigoQr.equals(codigo, ignoreCase = true) }
                if (coincidenciaLocal != null && !coincidenciaLocal.estadoPista.contains("HABILITADO", ignoreCase = true)) {
                    val avion = uiState.aeronaves.find { it.matricula.equals(coincidenciaLocal.matricula, ignoreCase = true) }
                    val iniSugerido = maxOf(
                        coincidenciaLocal.horometroInicial ?: 0.0,
                        avion?.horometroActual ?: 1245.2
                    )
                    val finSugerido = ((iniSugerido + 0.3) * 10.0).roundToInt() / 10.0
                    uiState = uiState.copy(
                        cargando = false,
                        vueloElegido = coincidenciaLocal,
                        codigoIngresado = coincidenciaLocal.codigoQr,
                        qrLeido = true,
                        qrVerificadoRemoto = false,
                        pistaDespejada = true,
                        combustibleOk = true,
                        lucesOk = true,
                        pilotoAlMando = coincidenciaLocal.pilotoAlMando.ifBlank { "Cap. Rodrigo Fuentes" },
                        horometroInicialTexto = String.format(Locale.US, "%.1f", iniSugerido),
                        horometroFinalTexto = String.format(Locale.US, "%.1f", finSugerido),
                        observaciones = "",
                        mensajeError = null,
                        mensajeQr = "✓ Código $codigo verificado en caché local"
                    )
                } else {
                    val yaUsado = coincidenciaLocal?.estadoPista?.contains("HABILITADO", ignoreCase = true) == true
                    uiState = uiState.copy(
                        cargando = false,
                        vueloElegido = null,
                        qrLeido = false,
                        qrVerificadoRemoto = false,
                        pilotoAlMando = "",
                        horometroInicialTexto = "",
                        horometroFinalTexto = "",
                        observaciones = "",
                        mensajeError = if (yaUsado) {
                            "Este código QR ($codigo) YA fue habilitado en pista anteriormente."
                        } else {
                            "Código QR '$codigo' no encontrado en el sistema CAPEBE."
                        }
                    )
                }
            }
        }
    }

    fun onPistaDespejadaChanged(valor: Boolean) {
        uiState = uiState.copy(pistaDespejada = valor, mensajeError = null)
    }

    fun onCombustibleOkChanged(valor: Boolean) {
        uiState = uiState.copy(combustibleOk = valor, mensajeError = null)
    }

    fun onLucesOkChanged(valor: Boolean) {
        uiState = uiState.copy(lucesOk = valor, mensajeError = null)
    }

    fun onPilotoAlMandoChanged(texto: String) {
        uiState = uiState.copy(pilotoAlMando = texto, mensajeError = null)
    }

    fun onHorometroInicialChanged(texto: String) {
        uiState = uiState.copy(horometroInicialTexto = texto.replace(",", "."), mensajeError = null)
    }

    fun onHorometroFinalChanged(texto: String) {
        uiState = uiState.copy(horometroFinalTexto = texto.replace(",", "."), mensajeError = null)
    }

    fun onObservacionesChanged(texto: String) {
        uiState = uiState.copy(observaciones = texto, mensajeError = null)
    }

    fun validarCondicionesPista(): Boolean {
        val s = uiState
        val vuelo = s.vueloElegido
        if (vuelo == null) {
            uiState = s.copy(mensajeError = "Selecciona un vuelo o busca un código QR primero.")
            return false
        }
        if (!s.qrLeido) {
            uiState = s.copy(mensajeError = "Debes validar el código QR / verificación del socio antes de aprobar.")
            return false
        }

        // Regla legal DGAC: aeronave en mantenimiento no puede despegar
        val avion = s.aeronaves.find { it.matricula.equals(vuelo.matricula, ignoreCase = true) }
        if (avion != null && (avion.enMantenimiento || avion.horometroActual >= avion.horometroMaxMantenimiento)) {
            uiState = s.copy(
                mensajeError = "BLOQUEO LEGAL DGAC: ${avion.matricula} alcanzó límite de mantenimiento (${avion.horometroMaxMantenimiento} hrs) y está FUERA DE VUELO."
            )
            return false
        }

        if (!s.pistaDespejada) {
            uiState = s.copy(mensajeError = "La pista y losa deben estar despejadas.")
            return false
        }
        if (!s.combustibleOk) {
            uiState = s.copy(mensajeError = "Se requiere combustible 100LL verificado.")
            return false
        }
        if (!s.lucesOk) {
            uiState = s.copy(mensajeError = "Las luces y balizas deben estar operativas.")
            return false
        }
        if (s.pilotoAlMando.trim().length < 4) {
            uiState = s.copy(mensajeError = "Ingresa el nombre del Piloto al Mando (requerido para bitácora DGAC).")
            return false
        }

        // Regla estricta Horómetro (4 dígitos + 1 decimal, siempre en aumento, <= 20 min)
        val iniTxt = s.horometroInicialTexto.trim()
        val finTxt = s.horometroFinalTexto.trim()
        if (!horometroRegex.matches(iniTxt) || !horometroRegex.matches(finTxt)) {
            uiState = s.copy(mensajeError = "El horómetro debe tener exactamente 4 dígitos y 1 decimal (ej: 1245.2).")
            return false
        }
        val ini = iniTxt.toDouble()
        val fin = finTxt.toDouble()
        if (avion != null && ini < avion.horometroActual) {
            uiState = s.copy(mensajeError = "Horómetro inicial ($ini) no puede ser menor al último registro oficial (${avion.horometroActual}).")
            return false
        }
        if (fin <= ini) {
            uiState = s.copy(mensajeError = "El horómetro siempre está en aumento: el valor final debe ser mayor al inicial.")
            return false
        }
        val delta = ((fin - ini) * 10.0).roundToInt() / 10.0
        if (delta > 0.4) {
            uiState = s.copy(mensajeError = "El vuelo recreativo no puede exceder 20 minutos (máx. 0.4 hrs de horómetro por bloque).")
            return false
        }
        if (s.observaciones.trim().length < 5) {
            uiState = s.copy(mensajeError = "Ingresa una observación de pista (mín. 5 caracteres).")
            return false
        }

        uiState = s.copy(mensajeError = null)
        return true
    }

    fun guardarVueloConfirmado(
        encargado: String = "Encargado Pista",
        onResultado: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val elegido = uiState.vueloElegido ?: return
        val pilotoTexto = uiState.pilotoAlMando.trim()
        val obsTexto = uiState.observaciones.trim()
        val pistaOk = uiState.pistaDespejada
        val combOk = uiState.combustibleOk
        val lucesOk = uiState.lucesOk
        val ini = uiState.horometroInicialTexto.toDoubleOrNull() ?: 1245.2
        val fin = uiState.horometroFinalTexto.toDoubleOrNull() ?: 1245.5
        val deltaMin = (((fin - ini) * 60.0)).roundToInt().coerceAtLeast(1)

        val habilitado = elegido.copy(
            pilotoAlMando = pilotoTexto,
            estadoPista = "QR VALIDADO - HABILITADO EN PISTA",
            encargadoPista = encargado,
            observaciones = obsTexto,
            pistaDespejada = pistaOk,
            combustibleOk = combOk,
            lucesOk = lucesOk,
            horometroInicial = ini,
            horometroFinal = fin,
            duracionMinutos = deltaMin
        )

        val flotaActualizada = uiState.aeronaves.map { avion ->
            if (avion.matricula.equals(elegido.matricula, ignoreCase = true)) {
                avion.copy(
                    horometroActual = fin,
                    enMantenimiento = avion.enMantenimiento || fin >= avion.horometroMaxMantenimiento
                )
            } else {
                avion
            }
        }

        uiState = uiState.copy(
            cargando = true,
            aeronaves = flotaActualizada,
            vuelos = uiState.vuelos.map { if (it.codigoQr.equals(elegido.codigoQr, ignoreCase = true)) habilitado else it },
            vuelosGuardados = listOf(habilitado) + uiState.vuelosGuardados.filterNot { it.codigoQr.equals(habilitado.codigoQr, ignoreCase = true) }
        )

        viewModelScope.launch {
            var syncPista = false
            var syncQr = false
            var errorServidor: String? = null

            // 1. Registrar vuelo + horómetro en service-pista (:8083)
            try {
                val idOp = if (elegido.idOperacion != null) {
                    elegido.idOperacion
                } else {
                    val creada = RetrofitPista.pistaApi.crearOperacion(
                        OperacionPistaDto(
                            fecha = elegido.fecha,
                            horario = elegido.horario,
                            horaLocal = elegido.horaLocal.ifBlank { elegido.horario.split("-").first().trim() },
                            base = elegido.base,
                            destino = elegido.destino,
                            aeronave = elegido.aeronave,
                            matricula = elegido.matricula,
                            pilotoAlMando = pilotoTexto,
                            pasajero = elegido.pasajero,
                            cantidadPasajeros = elegido.cantidadPasajeros,
                            codigoQr = elegido.codigoQr,
                            estadoPista = "PENDIENTE EN LOSA"
                        )
                    )
                    creada.idOperacion
                }

                if (idOp != null) {
                    RetrofitPista.pistaApi.registrarVueloYHorometro(
                        id = idOp,
                        datosVuelo = OperacionPistaDto(
                            fecha = elegido.fecha,
                            horario = elegido.horario,
                            horaLocal = elegido.horaLocal.ifBlank { elegido.horario.split("-").first().trim() },
                            base = elegido.base,
                            destino = elegido.destino,
                            aeronave = elegido.aeronave,
                            matricula = elegido.matricula,
                            pilotoAlMando = pilotoTexto,
                            pasajero = elegido.pasajero,
                            cantidadPasajeros = elegido.cantidadPasajeros,
                            codigoQr = elegido.codigoQr,
                            horometroInicial = ini,
                            horometroFinal = fin,
                            estadoPista = "HABILITADO EN PISTA"
                        )
                    )
                    syncPista = true
                }
            } catch (e: Exception) {
                errorServidor = e.localizedMessage
            }

            // 2. Validar QR en service-qr (:8084)
            try {
                RetrofitPista.qrApi.validarQrEnPista(
                    codigoQr = elegido.codigoQr,
                    datosValidacion = PaseQrDto(
                        codigoQr = elegido.codigoQr,
                        socio = elegido.pasajero,
                        rut = elegido.rut,
                        oficinaBanco = elegido.oficinaBanco,
                        esSocio = elegido.esSocio,
                        telefonoMovil = elegido.telefonoMovil,
                        cantidadPasajeros = elegido.cantidadPasajeros,
                        plazasAeronave = elegido.plazasAeronave,
                        fecha = elegido.fecha,
                        horario = elegido.horario,
                        base = elegido.base,
                        aeronave = elegido.aeronave,
                        destino = elegido.destino,
                        pilotoAlMando = pilotoTexto,
                        encargadoPista = encargado,
                        observaciones = obsTexto,
                        pistaDespejada = pistaOk,
                        combustibleOk = combOk,
                        lucesOk = lucesOk,
                        horometroInicial = ini,
                        horometroFinal = fin,
                        estadoQr = "QR VALIDADO - HABILITADO EN PISTA"
                    )
                )
                syncQr = true
            } catch (_: Exception) {}

            uiState = uiState.copy(
                cargando = false,
                conectadoPista = syncPista || uiState.conectadoPista,
                conectadoQr = syncQr || uiState.conectadoQr
            )
            // Limpiar completamente el formulario para que el siguiente vuelo empiece en blanco
            limpiarFormularioOperacion()
            cargarDesdeMicroservicios()
            onResultado(syncPista || syncQr, errorServidor)
        }
    }

    fun cargarReporteDiario(fecha: String? = null) {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            try {
                val reporte = RetrofitPista.pistaApi.obtenerReporteDiario(fecha)
                uiState = uiState.copy(
                    reporteDiario = reporte,
                    aeronaves = reporte.estadoFlotaMantenimiento.ifEmpty { uiState.aeronaves },
                    cargando = false,
                    conectadoPista = true
                )
            } catch (_: Exception) {
                // Respaldo local para reporte diario
                val vuelosLoc = uiState.vuelosGuardados.map {
                    OperacionPistaDto(
                        idOperacion = it.idOperacion,
                        fecha = it.fecha,
                        horario = it.horario,
                        horaLocal = it.horaLocal.ifBlank { it.horario.split("-").first().trim() },
                        base = it.base,
                        destino = it.destino,
                        aeronave = it.aeronave,
                        matricula = it.matricula,
                        pilotoAlMando = it.pilotoAlMando,
                        pasajero = it.pasajero,
                        cantidadPasajeros = it.cantidadPasajeros,
                        codigoQr = it.codigoQr,
                        horometroInicial = it.horometroInicial,
                        horometroFinal = it.horometroFinal,
                        duracionMinutos = it.duracionMinutos,
                        estadoPista = it.estadoPista
                    )
                }
                val totalHrs = vuelosLoc.sumOf {
                    if (it.horometroInicial != null && it.horometroFinal != null) {
                        (it.horometroFinal - it.horometroInicial).coerceAtLeast(0.0)
                    } else 0.0
                }
                uiState = uiState.copy(
                    reporteDiario = ReporteDiarioDto(
                        fechaReporte = fecha ?: "Resumen Local del Día",
                        destinatarios = listOf(
                            "Director de Operaciones (Bitácora oficial DGAC)",
                            "Director de Mantenimiento (Control de horas)"
                        ),
                        totalVuelosRealizados = vuelosLoc.size,
                        totalHorasVoladas = (totalHrs * 10.0).roundToInt() / 10.0,
                        notaLegalDgac = "Registro estricto de horómetro conforme al Código Aeronáutico y DGAC.",
                        vuelosRealizados = vuelosLoc,
                        estadoFlotaMantenimiento = uiState.aeronaves
                    ),
                    cargando = false
                )
            }
        }
    }

    private fun crearFlotaRespaldo(): List<AeronaveDto> = listOf(
        AeronaveDto(1L, "CC-AAA", "Cessna 172", 4, 3, "Tobalaba", 1245.2, 1250.0, false),
        AeronaveDto(2L, "CC-BBB", "Piper PA-28", 4, 3, "Tobalaba", 2198.4, 2200.0, false),
        AeronaveDto(3L, "CC-DDD", "Cessna 206", 6, 5, "Talcahuano", 3410.0, 3450.0, false),
        AeronaveDto(4L, "CC-EEE", "Piper PA-32 Saratoga", 6, 5, "Tobalaba", 4100.0, 4100.0, true)
    )

    private fun crearVuelosRespaldo(): List<VueloModel> = listOf(
        VueloModel(1L, "2026-11-21", "10:00 - 11:00", "10:15", "Tobalaba", "Circuito Precordillera Santiago (SCTB)", "CC-AAA Cessna 172", "CC-AAA", 4, "Cap. Rodrigo Fuentes", "Álvaro Ramírez Soto", "18.452.310-K", "Casa Matriz Alameda", true, "+56987654321", 3, "CAPEBE-SCTB-1001", "PENDIENTE EN LOSA", horometroInicial = 1245.2),
        VueloModel(2L, "2026-11-21", "11:00 - 12:00", "11:10", "Tobalaba", "Circuito Cordillera Oriente (SCTB)", "CC-BBB Piper PA-28", "CC-BBB", 4, "Cap. Marcela Paredes", "Camila Morales Vega", "17.920.445-3", "Sucursal Providencia", true, "+56976543210", 2, "CAPEBE-SCTB-1002", "PENDIENTE EN LOSA", horometroInicial = 2198.4),
        VueloModel(3L, "2026-11-22", "09:00 - 10:00", "09:20", "Talcahuano", "Circuito Bahía Concepción (SCIE)", "CC-DDD Cessna 206", "CC-DDD", 6, "Cap. Javier Muñoz", "Cristián Herrera Díaz", "16.512.889-7", "Sucursal Concepción Centro", false, "+56965432109", 4, "CAPEBE-SCIE-1003", "PENDIENTE EN LOSA", horometroInicial = 3410.0),
        VueloModel(4L, "2026-11-28", "15:00 - 16:00", "15:15", "Tobalaba", "Circuito Precordillera Santiago (SCTB)", "CC-DDD Cessna 206", "CC-DDD", 6, "Cap. Rodrigo Fuentes", "Francisca Rojas Silva", "19.210.774-1", "Sucursal Las Condes", true, "+56954321098", 5, "CAPEBE-SCTB-1004", "PENDIENTE EN LOSA", horometroInicial = 3410.3),
        VueloModel(5L, "2026-11-29", "10:00 - 11:00", "10:15", "Talcahuano", "Circuito Península Tumbes (SCIE)", "CC-AAA Cessna 172", "CC-AAA", 4, "Cap. Javier Muñoz", "Matías Contreras Pino", "15.803.112-4", "Sucursal Talcahuano", true, "+56943210987", 3, "CAPEBE-SCIE-1005", "PENDIENTE EN LOSA", horometroInicial = 1245.5)
    )
}
