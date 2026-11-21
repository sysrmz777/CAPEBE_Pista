# App Encargado de Pista — CAPEBE

Aplicación móvil Android (**Kotlin + Jetpack Compose + MVVM**) desarrollada para el **Encargado de Pista** del Club Aéreo del Personal del BancoEstado (CAPEBE), operando en las bases **Aeródromo Tobalaba (SCTB)** y **Carriel Sur Talcahuano (SCIE)**.

## Funcionalidades Principales
- **Autenticación con SQLite (Room) + PIN de 4 dígitos:** Registro, inicio de sesión y desbloqueo rápido en losa.
- **Calendario Operativo de Vuelos:** Sincronización en tiempo real con los microservicios en AWS EC2 (`3.213.85.189` en puertos `8082`, `8083` y `8084`), filtro por fines de semana/festivos y exportación a Google Calendar.
- **Escáner QR Real con Cámara (ZXing):** Lectura directa con cámara del pase QR emitido por la App del Socio (y entrada manual como respaldo), validación remota contra PostgreSQL y control de QR de un solo uso.
- **Checklist Normativo y Horómetro DGAC:** Verificación de pista/combustible/luces y registro estricto de horómetro (`XXXX.X`, siempre en aumento, máximo 20 minutos por vuelo recreativo y bloqueo automático si la aeronave alcanzó límite de mantenimiento).
- **Bitácora y Reporte Diario DGAC:** Historial de vuelos habilitados y cierre diario consolidado por matrícula, horas voladas y estado de mantenimiento de la flota.
- **Instrumental Nativo de Losa:**
  - **GPS en vivo y Mapa OpenStreetMap (`osmdroid`):** Posición real con `FusedLocationProviderClient`, latitud, longitud, altitud, precisión, velocidad (`km/h` y `kt`), distancia/rumbo a SCTB y SCIE, y apertura directa en **Google Maps**.
  - **Linterna baliza:** Control del flash LED de la cámara.
  - **Brújula magnética:** Rumbo en grados y orientación cardinal usando los sensores del dispositivo.

## Stack Tecnológico
- **Lenguaje y UI:** Kotlin, Jetpack Compose, Material Design 3, Navigation Compose.
- **Arquitectura:** MVVM (`data/database`, `data/local`, `data/remote`, `model`, `ui/components`, `ui/navigation`, `ui/screen`, `ui/viewModel`).
- **Persistencia local:** Room (SQLite) con KSP.
- **Red / Backend Cloud:** Retrofit 2 + Gson + OkHttp contra Spring Boot + PostgreSQL 18 en AWS EC2 (`ServerConfig.kt`).
- **Mapas, GPS y QR:** Google Play Services Location, `osmdroid`, `zxing-android-embedded`.

## Ejecución en Android Studio
1. Clonar el repositorio y abrir la carpeta del proyecto en Android Studio.
2. Ejecutar **Sync Project with Gradle Files**.
3. Compilar o instalar en un dispositivo Android físico (Android 8.0 / API 26 o superior).
