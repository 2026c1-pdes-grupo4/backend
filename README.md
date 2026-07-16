# CTH Backend — Compra Tu Hogar

[![CI/CD](https://github.com/2026c1-pdes-grupo4/backend/actions/workflows/ci.yml/badge.svg)](https://github.com/2026c1-pdes-grupo4/backend/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=2026c1-pdes-grupo4_backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=2026c1-pdes-grupo4_backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=2026c1-pdes-grupo4_backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=2026c1-pdes-grupo4_backend)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)

API REST para **Compra Tu Hogar**.

---

## 📋 Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3 |
| Seguridad | Spring Security + JWT (jjwt 0.11) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8.4 |
| Mapeo | MapStruct 1.5 |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Mockito + Spring Boot Test |
| Cobertura | JaCoCo (mínimo 75%) |
| Calidad | SonarCloud |
| Load Testing | k6 |
| Observabilidad | Actuator + Micrometer + Prometheus + Grafana + Loki |
| Contenedores | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Registry | GitHub Container Registry (GHCR) |

---

## 🚀 Levantar el proyecto

### ▶️ Opción A — Solo Docker

Solo necesitás **Docker Desktop** instalado y corriendo. No se requiere Java ni Maven.

```bash
docker compose -f docker-compose.prod.yml up -d
```

Docker descarga automáticamente la imagen publicada en GHCR y levanta la API junto con MySQL.


### ▶️ Opción B — Desde el código fuente (desarrollo)

Requiere Java 21+ y Maven 3.9+.

**1. Compilar el JAR**

```bash
mvn clean package -DskipTests
```

**2. Levantar app + base de datos**

```bash
docker compose up --build -d
```

---

Servicios levantados (ambas opciones):

| Contenedor | Puerto | Descripción |
|---|---|---|
| `cth-mysql` | `3306` | MySQL 8.4 |
| `cth-api` | `8080` | API REST |

La API queda disponible en `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

#### Usuarios de prueba

| Rol | Usuario | Contraseña |
|---|---|---|
| Admin | `admin123` | `admin123` |
| Admin | `karina` | `admin123` |
| Buyer | `buyer1` | `buyer123` |
| Buyer | `buyer2` | `buyer123` |
| Agency | `inmo1` | `agency123` |
| Agency | `inmo2` | `agency123` |
| Agency | `ritondo_propiedades` | `agency123` |
| Agency | `puerto_madero_brokers` | `agency123` |

---

## 🔍 Observabilidad

### Levantar stack de observabilidad

```bash
# Primero levantá la app (necesaria para que exista la red cth-net)
docker compose up -d

# Luego el stack de métricas y logs
docker compose -f docker-compose.observability.yml up -d
```

| Servicio | URL | Credenciales |
|---|---|---|
| **Grafana** | `http://localhost:3000` | admin / admin |
| **Prometheus** | `http://localhost:9090` | — |
| **Loki** | `http://localhost:3100` | — |
| **Actuator Health** | `http://localhost:8080/actuator/health` | — |
| **Prometheus Metrics** | `http://localhost:8080/actuator/prometheus` | — |

El dashboard **CTH Backend — Observability** se provisionea automáticamente en Grafana con:
- Request rate por endpoint
- **Error rate (%) — alerta en > 5%**
- Latencia P95 / P50
- JVM Heap memory
- Stream de logs en tiempo real (Loki)

Cada request incluye un header `X-Correlation-Id` para trazabilidad en los logs.

---

## 🧪 Tests

### Tests unitarios y de integración

```bash
# Correr todos los tests
mvn test

# Correr tests + verificar cobertura (mínimo 75%)
mvn verify
```

El reporte JaCoCo queda en `target/site/jacoco/index.html`.

### Load Tests (k6)

Requiere la app corriendo en `http://localhost:8080`.

```powershell
# Windows — todos los tests
.\k6\run-tests.ps1 all

# Test individual
.\k6\run-tests.ps1 auth    # 20 VUs — autenticación
.\k6\run-tests.ps1 search  # 15 VUs — búsqueda de propiedades
.\k6\run-tests.ps1 stress  # 100 VUs — stress de compras
```

Thresholds configurados:
- `p(95) < 500ms` — el 95% de los requests deben responder en menos de 500ms
- `error_rate < 5%` — tasa de error menor al 5%

---

## 📊 SonarCloud

El análisis de calidad se realiza en [SonarCloud](https://sonarcloud.io/summary/new_code?id=2026c1-pdes-grupo4_backend).

La configuración está centralizada en el `pom.xml` (`sonar.projectKey`, `sonar.organization`, `sonar.host.url`).

```bash
# Correr análisis manualmente (requiere token de SonarCloud)
mvn clean verify sonar:sonar -Dsonar.token=<TU_TOKEN>
```

> 💡 En CI/CD el token se inyecta automáticamente desde el secret `SONAR_TOKEN` del repositorio.

---

## 🔧 Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de la base de datos | `jdbc:mysql://localhost:3306/cth` |
| `SPRING_DATASOURCE_USERNAME` | Usuario MySQL | `cth` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña MySQL | `1234` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `mysql` |
| `APP_JWT_SECRET` | Secret para firmar JWT (mín. 256 bits) | ver `application.properties` |
| `APP_JWT_EXPIRATIONMINUTES` | Expiración del token en minutos | `1440` |
| `APP_CORS_ALLOWEDORIGIN` | Origen permitido para CORS | `http://localhost:5173` |

---

## 🐳 Arquitectura Docker

```
┌─────────────────────────────────────────────────┐
│                    cth-net                      │
│                                                 │
│  ┌──────────┐      ┌──────────┐                 │
│  │ cth-api  │─────►│cth-mysql │                 │
│  │  :8080   │      │  :3306   │                 │
│  └────┬─────┘      └──────────┘                 │
│       │  expone /actuator/prometheus             │
│       ▼                                         │
│  ┌──────────────┐  ┌──────────┐  ┌───────────┐  │
│  │  prometheus  │  │   loki   │  │  grafana  │  │
│  │    :9090     │  │  :3100   │  │   :3000   │  │
│  └──────────────┘  └──────────┘  └───────────┘  │
└─────────────────────────────────────────────────┘
```

Archivos Docker Compose:

| Archivo | Propósito |
|---|---|
| `docker-compose.prod.yml` |  usa imagen de GHCR, solo necesita Docker |
| `docker-compose.yml` | Desarrollo local — construye la imagen desde el código fuente |
| `docker-compose.observability.yml` | Prometheus + Grafana + Loki |

---

## 🔀 Git Flow

```
main        ← producción (releases estables)
develop     ← integración (rama base para features)
feature/*   ← nuevas funcionalidades (salen de develop)
hotfix/*    ← fixes urgentes (salen de main)
```

**Flujo de trabajo:**

```
feature/* ──► PR ──► develop ──► PR ──► main
                                    │
                               dispara pipeline completo
                               (build + sonar opcional + k6 + push + deploy)
```

---

## ⚙️ Pipeline CI/CD

El pipeline se define en `.github/workflows/ci.yml` y corre en cada push a `main`/`develop` y en PRs:

```
Job 1: Build & Test  ──────────────────────────────────────
            │                     │                    │
            ▼                     ▼                    ▼
    Job 2: SonarCloud    Job 3: Load Tests    Job 4: Push GHCR
                              (k6)             (solo push)
                                                    │
                                               (solo main)
                                                    ▼
                                          Job 5: Deploy 🚧
```

La imagen Docker se publica en `ghcr.io/2026c1-pdes-grupo4/backend`.

---

## 📌 API Error Format

Formato de error uniforme para el frontend:

```json
{
  "timestamp": "2026-05-31T20:20:20Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_REQUEST",
  "message": "Request validation failed",
  "path": "/properties/search",
  "details": ["priceMin > priceMax"]
}
```

El catálogo de códigos funcionales está centralizado en `ErrorCode.java`.
