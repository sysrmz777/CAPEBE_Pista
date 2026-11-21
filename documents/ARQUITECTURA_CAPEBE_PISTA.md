# Diagramas de Arquitectura - Proyecto CAPEBE (Rol Operador de Pista)

Documento preparado para la rama `/documents` según la **Rúbrica Experiencia 3 - 2026**.

---

## 1. Diagrama de Arquitectura del Proyecto (MVVM + SQLite + Retrofit + Cloud)

```mermaid
flowchart TD
    subgraph Android["📱 App Android PISTA_CAPEBE (Jetpack Compose + MVVM)"]
        UI["Compose UI (Pantallas Rol Operador de Pista)\n• PantallaLogin / Login / Register / PinScreen\n• Principal (Jefatura de Pista)\n• CalendarioVuelos (Control Pista)\n• TerminosCondiciones (Protocolo DGAC)\n• ConfirmacionVuelo (Autorización)\n• MisVuelos (Bitácora e Inspecciones)\n• RecursosNativos (Linterna, Batería, Vibración, GPS, Brújula)"]
        VM["ViewModels (Lógica y Estado UI)\n• LoginViewModel\n• RegisterViewModel\n• CalendarioViewModel"]
        ROOM["SQLite Local (Room + SessionManager)\n• UsuarioEntity / UsuarioDao\n• capebe_pista_database.db\n• Autenticación y PIN 4 dígitos"]
        RETRO["Cliente HTTP Retrofit 2 + OkHttp\n• RetrofitPista\n• PistaApi (Puerto 8080)\n• InspeccionApi (Puerto 8081)"]
        NATIVOS["Recursos Nativos Android\n• CameraManager (Linterna Losa)\n• BatteryManager (Batería Turno)\n• VibratorManager (Alerta Háptica)\n• LocationManager (GPS Mapa OSM)\n• SensorManager (Brújula Magnética)"]

        UI --> VM
        UI --> NATIVOS
        VM --> ROOM
        VM --> RETRO
    end

    subgraph Cloud["☁️ Nube (AWS EC2 / Docker Compose)"]
        subgraph Docker1["Contenedor Microservicio 1 (Puerto 8080)"]
            MS1["service-pista (Spring Boot + JPA)\n/api/v1/pista"]
        end
        subgraph Docker2["Contenedor Microservicio 2 (Puerto 8081)"]
            MS2["service-inspecciones (Spring Boot + JPA)\n/api/v1/inspecciones"]
        end
        subgraph DBs["Contenedores PostgreSQL 18"]
            PG1[("PostgreSQL (5434:5432)\ncapebe_pista_db")]
            PG2[("PostgreSQL (5435:5432)\ncapebe_inspecciones_db")]
        end

        MS1 --> PG1
        MS2 --> PG2
    end

    RETRO -- "HTTP / JSON (GET, POST, PUT, DELETE)" --> MS1
    RETRO -- "HTTP / JSON (GET, POST, PUT, DELETE)" --> MS2
```

---

## 2. Diagrama de Arquitectura de Microservicios (Spring Boot + JUnit 5 + PostgreSQL)

```mermaid
flowchart LR
    subgraph Cliente["App Android (Operador de Pista)"]
        Retrofit["RetrofitPista\n(GsonConverter + LoggingInterceptor)"]
    end

    subgraph MS_Pista["Microservicio 1: service-pista (Puerto 8080)"]
        C1["OperacionPistaController\n@RestController /api/v1/pista"]
        S1["OperacionPistaService\n@Service @Transactional"]
        R1["OperacionPistaRepository\nJpaRepository<OperacionPista, Long>"]
        E1["OperacionPista (@Entity)\n• idOperacion\n• codigoPista (SCTB/SCIE)\n• aeronave (CC-AAA/BBB/CCC)\n• pilotoAsignado\n• fecha / horario\n• estadoPista\n• condicionClima"]
        EX1["GlobalExceptionHandler\n@RestControllerAdvice"]
        T1["OperacionPistaServiceTest\nJUnit 5 + Mockito"]

        C1 --> S1
        S1 --> R1
        R1 --> E1
        T1 -. "Prueba Unitaria" .-> S1
    end

    subgraph MS_Inspecciones["Microservicio 2: service-inspecciones (Puerto 8081)"]
        C2["InspeccionPistaController\n@RestController /api/v1/inspecciones"]
        S2["InspeccionPistaService\n@Service @Transactional"]
        R2["InspeccionPistaRepository\nJpaRepository<InspeccionPista, Long>"]
        E2["InspeccionPista (@Entity)\n• idInspeccion\n• matriculaAeronave\n• basePista\n• fechaHora\n• nivelCombustible (100LL)\n• estadoChecklist\n• lucesYBalizasOk\n• observaciones\n• operadorPista"]
        EX2["GlobalExceptionHandler\n@RestControllerAdvice"]
        T2["InspeccionPistaServiceTest\nJUnit 5 + Mockito"]

        C2 --> S2
        S2 --> R2
        R2 --> E2
        T2 -. "Prueba Unitaria" .-> S2
    end

    DB1[("PostgreSQL\ncapebe_pista_db")]
    DB2[("PostgreSQL\ncapebe_inspecciones_db")]

    Retrofit --> C1
    Retrofit --> C2
    R1 --> DB1
    R2 --> DB2
```
