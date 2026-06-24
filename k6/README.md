# Tests de Carga y Stress con k6

Scripts de performance testing para el backend de CTH.

## Instalación de k6

### Windows
```bash
winget install k6 --source winget
```

## Pre-requisitos

La API debe estar corriendo en `http://localhost:8080` con los datos del seed cargados.

```bash
# Levantar la app con Docker Compose
docker-compose up -d
```

## Scripts disponibles

### 1. `load-auth.js` — Load test de autenticación
Prueba el endpoint `POST /auth/login` con ramp-up de 1 a 20 VUs durante ~90s.

```bash
k6 run k6/load-auth.js
```

| Métrica | Threshold |
|---|---|
| p(95) de latencia | < 500ms |
| Tasa de error | < 5% |

---

### 2. `load-search.js` — Load test de búsqueda de propiedades
Prueba `GET /properties/search` con múltiples combinaciones de filtros bajo carga de hasta 15 VUs.

```bash
k6 run k6/load-search.js
```

| Métrica | Threshold |
|---|---|
| p(95) de latencia | < 800ms |
| Tasa de error | < 5% |

---

### 3. `stress-purchase.js` — Stress test de compras
Escala rápidamente hasta 100 VUs simultáneos probando `GET /purchases/me` para identificar el punto de quiebre.

```bash
k6 run k6/stress-purchase.js
```

| Métrica | Threshold |
|---|---|
| p(95) de latencia | < 1000ms |
| Tasa de error | < 10% |

---

## Parámetros de entorno

Podés cambiar la URL base con la variable de entorno `BASE_URL`:

```bash
k6 run -e BASE_URL=http://mi-servidor:8080 k6/load-auth.js
```

## Guardar resultados

```bash
# Exportar resultados a JSON
k6 run --out json=resultados.json k6/load-auth.js

# Enviar métricas a Grafana (cuando el stack de observabilidad está levantado)
k6 run --out influxdb=http://localhost:8086/k6 k6/load-auth.js
```

