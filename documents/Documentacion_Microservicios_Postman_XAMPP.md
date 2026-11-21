# Documentación Técnica: Microservicios CAPEBE (XAMPP + Spring Boot + Postman)

**Proyecto:** Sistema de Operaciones de Vuelo y Pase de Abordaje QR — Club Aéreo del Personal del BancoEstado (CAPEBE)  
**Grupo:** Grupo 10  
**Arquitectura:** Microservicios RESTful con Spring Boot 4.0.5, JPA/Hibernate y MySQL (XAMPP)  
**Aplicaciones Móviles Conectadas:** `VUELOS_CAPEBE` (Rol Socio/Cliente) y `PISTA_CAPEBE` (Rol Encargado de Pista)

---

## 1. Resumen de la Arquitectura de Microservicios

El ecosistema backend está compuesto por **dos microservicios independientes** que se comunican con las aplicaciones móviles Android mediante **Retrofit2** y persisten su información en bases de datos relacionales **MySQL** gestionadas con **XAMPP**:

| Microservicio | Puerto | Base de Datos MySQL (XAMPP) | Rol Principal en el Negocio |
| :--- | :---: | :--- | :--- |
| **`service-pista`** | `8080` | `capebe_pista_db` | Gestión de operaciones de pista, programación de vuelos por aeródromo (`Tobalaba` / `Talcahuano`) y habilitación oficial de despegue. |
| **`service-qr`** | `8081` | `capebe_qr_db` | Generación del código QR de embarque cuando el Socio reserva un vuelo (`VUELOS_CAPEBE`) y lectura/validación del QR con checklist técnico y observaciones por el Encargado de Pista (`PISTA_CAPEBE`). |

---

## 2. Configuración con XAMPP (MySQL)

Ambos microservicios están configurados para conectarse automáticamente al motor **MySQL** de **XAMPP** en el puerto `3306` utilizando el parámetro `createDatabaseIfNotExist=true`.

### Pasos de Ejecución
1. Abrir **XAMPP Control Panel** e iniciar los módulos **MySQL** (puerto `3306`) y **Apache** (para visualizar las tablas en `http://localhost/phpmyadmin`).
2. Ejecutar `Grupo10Application.java` en el proyecto **`service-pista`** (se levanta en `http://localhost:8080` y crea automáticamente la base `capebe_pista_db` con 5 vuelos iniciales).
3. Ejecutar `Grupo10Application.java` en el proyecto **`service-qr`** (se levanta en `http://localhost:8081` y crea automáticamente la base `capebe_qr_db` con 3 pases QR iniciales).

### Parámetros de Conexión (`application.properties`)

#### Microservicio 1: `service-pista`
```properties
spring.application.name=service-pista
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/capebe_pista_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

#### Microservicio 2: `service-qr`
```properties
spring.application.name=service-qr
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/capebe_qr_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 3. Reglas de Negocio Implementadas

1. **Bloqueo de Pista Cerrada (`service-pista`):**
   - Al invocar el endpoint `PUT /api/v1/pista/{id}/habilitar`, el servicio verifica el estado actual de la pista. Si el estado es `"CERRADA"`, rechaza la operación con `400 Bad Request` (`"No se puede habilitar un vuelo en una pista cerrada"`). Si está disponible, actualiza el estado a `"HABILITADO EN PISTA"`.
2. **Generación de Código Único por Aeródromo (`service-qr`):**
   - Al reservar un vuelo en `POST /api/v1/qr/generar`, el servicio asigna el prefijo aeronáutico OACI según la base:
     - `CAPEBE-SCTB-XXXX` para el Aeródromo Eulogio Sánchez (**Tobalaba**).
     - `CAPEBE-SCIE-XXXX` para el Aeródromo Carriel Sur (**Talcahuano**).
3. **Validación de Seguridad e Impedimento de Doble Uso de QR (`service-qr`):**
   - Al escanear/validar un QR en `PUT /api/v1/qr/validar/{codigoQr}`:
     - Si el pase ya tiene estado `"QR VALIDADO - HABILITADO EN PISTA"`, el microservicio lanza una excepción (`400 Bad Request`) impidiendo reutilizar un pase de abordaje ya consumido.
     - Evalúa las 3 condiciones físicas de pista (`pistaDespejada`, `combustibleOk`, `lucesOk`) junto con las `observaciones` del encargado:
       - Si las 3 están en `true`: el estado pasa a `"QR VALIDADO - HABILITADO EN PISTA"`.
       - Si alguna está en `false`: el estado pasa a `"OBSERVADO EN PISTA - PENDIENTE"`.

---

## 4. Guía de Pruebas en Postman

> **Nota para Postman:** En las peticiones `POST` y `PUT` que envían datos, seleccionar la pestaña **Body** -> marcar **raw** -> seleccionar formato **JSON**.

---

### 4.1. Microservicio 1: `service-pista` (`http://localhost:8080`)

| Método | Endpoint | Descripción |
| :---: | :--- | :--- |
| `GET` | `/api/v1/pista` | Lista todas las operaciones de pista registradas. |
| `GET` | `/api/v1/pista/{id}` | Obtiene el detalle de una operación por su ID. |
| `GET` | `/api/v1/pista/base/{base}` | Filtra las operaciones por base (`Tobalaba` o `Talcahuano`). |
| `POST` | `/api/v1/pista` | Registra una nueva operación de vuelo en pista. |
| `PUT` | `/api/v1/pista/{id}` | Actualiza los datos completos de una operación de pista. |
| `PUT` | `/api/v1/pista/{id}/habilitar` | Aplica la regla de negocio para habilitar el vuelo en pista. |
| `DELETE` | `/api/v1/pista/{id}` | Elimina una operación de pista por su ID. |

#### Ejemplos de Peticiones en Postman (`service-pista`)

**1. Listar todos los vuelos (`GET`)**
- **URL:** `http://localhost:8080/api/v1/pista`

**2. Filtrar vuelos por base (`GET`)**
- **URL:** `http://localhost:8080/api/v1/pista/base/Tobalaba`

**3. Crear nueva operación de pista (`POST`)**
- **URL:** `http://localhost:8080/api/v1/pista`
- **Body (JSON):**
```json
{
  "fecha": "Domingo 01/11/2026",
  "horario": "15:00 - 16:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)",
  "estadoPista": "PENDIENTE"
}
```

**4. Habilitar operación de pista (`PUT`)**
- **URL:** `http://localhost:8080/api/v1/pista/1/habilitar`
- **Respuesta esperada (`200 OK`):**
```json
{
  "idOperacion": 1,
  "fecha": "Sábado 24/10/2026",
  "horario": "09:00 - 10:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)",
  "estadoPista": "HABILITADO EN PISTA"
}
```

**5. Eliminar operación de pista (`DELETE`)**
- **URL:** `http://localhost:8080/api/v1/pista/1`

---

### 4.2. Microservicio 2: `service-qr` (`http://localhost:8081`)

| Método | Endpoint | Descripción |
| :---: | :--- | :--- |
| `GET` | `/api/v1/qr` | Lista todos los pases de abordaje QR emitidos. |
| `GET` | `/api/v1/qr/{id}` | Consulta un pase QR por su ID numérico. |
| `GET` | `/api/v1/qr/codigo/{codigoQr}` | Busca un pase QR por su código alfanumérico (ej. `CAPEBE-SCTB-1001`). |
| `POST` | `/api/v1/qr/generar` | Genera un nuevo código QR cuando el socio confirma un vuelo. |
| `PUT` | `/api/v1/qr/validar/{codigoQr}` | Valida el QR en pista registrando checklist y observaciones. |
| `DELETE` | `/api/v1/qr/{id}` | Elimina un pase QR por su ID. |

#### Ejemplos de Peticiones en Postman (`service-qr`)

**1. Listar todos los pases QR (`GET`)**
- **URL:** `http://localhost:8081/api/v1/qr`

**2. Consultar un QR por su código (`GET`)**
- **URL:** `http://localhost:8081/api/v1/qr/codigo/CAPEBE-SCTB-1001`

**3. Generar nuevo QR desde la app de Socio (`POST`)**
- **URL:** `http://localhost:8081/api/v1/qr/generar`
- **Body (JSON):**
```json
{
  "socio": "Cliente CAPEBE",
  "fecha": "Sábado 24/10/2026",
  "horario": "09:00 - 10:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)"
}
```
- **Respuesta esperada (`201 Created`):**
```json
{
  "idQr": 4,
  "codigoQr": "CAPEBE-SCTB-4821",
  "socio": "Cliente CAPEBE",
  "fecha": "Sábado 24/10/2026",
  "horario": "09:00 - 10:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)",
  "encargadoPista": "SIN ASIGNAR",
  "observaciones": "SIN OBSERVACIONES",
  "pistaDespejada": false,
  "combustibleOk": false,
  "lucesOk": false,
  "estadoQr": "QR GENERADO - PENDIENTE EN PISTA"
}
```

**4. Validar QR y registrar observaciones desde la app de Pista (`PUT`)**
- **URL:** `http://localhost:8081/api/v1/qr/validar/CAPEBE-SCTB-1001`
- **Body (JSON):**
```json
{
  "codigoQr": "CAPEBE-SCTB-1001",
  "socio": "Socio CAPEBE",
  "fecha": "Sábado 24/10/2026",
  "horario": "09:00 - 10:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)",
  "encargadoPista": "Encargado Pista",
  "observaciones": "Pista revisada, viento en calma y combustible 100LL completo",
  "pistaDespejada": true,
  "combustibleOk": true,
  "lucesOk": true,
  "estadoQr": "QR GENERADO - PENDIENTE EN PISTA"
}
```
- **Respuesta esperada (`200 OK`):**
```json
{
  "idQr": 1,
  "codigoQr": "CAPEBE-SCTB-1001",
  "socio": "Socio CAPEBE",
  "fecha": "Sábado 24/10/2026",
  "horario": "09:00 - 10:00",
  "base": "Tobalaba",
  "aeronave": "Cessna 172N (CC-KST)",
  "encargadoPista": "Encargado Pista",
  "observaciones": "Pista revisada, viento en calma y combustible 100LL completo",
  "pistaDespejada": true,
  "combustibleOk": true,
  "lucesOk": true,
  "estadoQr": "QR VALIDADO - HABILITADO EN PISTA"
}
```

**5. Eliminar un pase QR (`DELETE`)**
- **URL:** `http://localhost:8081/api/v1/qr/1`

---

## 5. Pruebas Unitarias (JUnit 5 + Mockito)

Ambos microservicios cuentan con pruebas unitarias aisladas en su capa `service`:
- **`OperacionPistaServiceTest.java` (`service-pista`):** Verifica listado, búsqueda por ID, creación, habilitación exitosa de pista y rechazo mediante excepción cuando la pista se encuentra `"CERRADA"`.
- **`PaseQrServiceTest.java` (`service-qr`):** Verifica listado, generación de código QR con prefijo `"CAPEBE-SCTB-"`, validación exitosa pasando a `"QR VALIDADO - HABILITADO EN PISTA"`, estado `"OBSERVADO EN PISTA - PENDIENTE"` cuando falta un requisito técnico y bloqueo cuando un QR ya fue validado previamente.
