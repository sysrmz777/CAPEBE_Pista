# Diagramas de Arquitectura — App Encargado de Pista CAPEBE

Documento técnico para `/documents` — **Club Aéreo del Personal del BancoEstado (CAPEBE)**.

---

## 1. Arquitectura General (Android MVVM + SQLite + Retrofit + AWS EC2)

```mermaid
flowchart TD
    subgraph Android["App Android: Encargado de Pista CAPEBE (Kotlin + Jetpack Compose + MVVM)"]
        UI["Pantallas Compose\n• PantallaLogin / Login / Register / PinScreen\n• Principal (Centro de Control de Pista)\n• CalendarioVuelos (Sáb/Dom/Festivos)\n• TerminosCondiciones + QrScannerActivity (Cámara ZXing)\n• ConfirmacionVuelo (Horómetro DGAC)\n• MisVuelos (Bitácora) y ReporteDiarioScreen\n• RecursosNativos (Linterna, GPS OSM, Brújula)"]
        VM["ViewModels\n• LoginViewModel\n• RegisterViewModel\n• CalendarioViewModel"]
        ROOM["Persistencia Local (Room SQLite)\n• AppDatabase / UsuarioDao / UsuarioEntity\n• SessionManager + PIN 4 dígitos"]
        RETRO["Cliente REST (Retrofit 2 + OkHttp)\n• ServerConfig (AWS EC2)\n• PistaApi (:8083) y VuelosSocioApi (:8082)\n• QrApi (:8084)"]
        NATIVOS["Hardware y Sensores Nativos\n• Cámara (ZXing Embedded QR Scanner)\n• GPS FusedLocation + osmdroid OSM + Google Maps\n• CameraManager (Linterna de losa)\n• SensorManager (Brújula magnética)"]

        UI --> VM
        UI --> NATIVOS
        VM --> ROOM
        VM --> RETRO
    end

    subgraph Cloud["Nube AWS EC2 (Docker Compose)"]
        MSV["service-vuelos (:8082)\n/api/vuelos"]
        MSP["service-pista (:8083)\n/api/v1/pista"]
        MSQ["service-qr (:8084)\n/api/v1/qr"]
        PG[("PostgreSQL 18 Alpine\nBase de datos capebeBD")]

        MSV --> PG
        MSP --> PG
        MSQ --> PG
    end

    RETRO -- "HTTP / JSON" --> MSV
    RETRO -- "HTTP / JSON" --> MSP
    RETRO -- "HTTP / JSON" --> MSQ
```

---

## 2. Flujo de Validación QR y Habilitación en Pista

```mermaid
sequenceDiagram
    actor Encargado as Encargado de Pista
    participant App as App Pista CAPEBE
    participant Cam as Cámara QR (ZXing)
    participant QR as service-qr (:8084)
    participant Pista as service-pista (:8083)
    participant DB as PostgreSQL (AWS)

    Encargado->>App: Toca "Escanear Código QR con Cámara"
    App->>Cam: Abre QrScannerActivity (vertical)
    Cam-->>App: Devuelve código (ej. CAPEBE-SCTB-XXXX)
    App->>QR: GET /api/v1/qr/codigo/{codigo}
    QR->>DB: Consulta estado y datos del pasajero
    DB-->>QR: Pase válido (PENDIENTE)
    QR-->>App: Identidad, aeronave, horario y cupos
    Encargado->>App: Completa checklist losa + horómetro DGAC (XXXX.X)
    App->>Pista: PUT /api/v1/pista/{id}/horometro
    Pista->>DB: Registra vuelo y actualiza horómetro aeronave
    App->>QR: PUT /api/v1/qr/validar/{codigo}
    QR->>DB: Marca QR como VALIDADO - HABILITADO EN PISTA
    App-->>Encargado: Confirmación guardada y formulario limpio para el siguiente vuelo
```
