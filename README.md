# CTH Backend API

API REST desarrollada con **Spring Boot 3**, **MySQL 8.4** y **JWT**.

---

## Requisitos

- Docker instalado y corriendo
- Maven 
- Java 21+

---

## Levantar el proyecto con Docker

### 1. Compilar el proyecto

```bash
mvn clean package -DskipTests
```

Esto genera el archivo `target/cth-0.0.1-SNAPSHOT.jar`.

### 2. Levantar los contenedores

```bash
docker-compose up --build
```

Esto levanta dos servicios:
- **cth-mysql**:  MySQL en el puerto `3306`
- **cth-api**: la API en el puerto `8080`

---

la API está disponible en:

```
http://localhost:8080
```

---

## Tests y cobertura

```bash
mvn test
```

Para validar cobertura (JaCoCo, umbral global >= 80%):

```bash
mvn verify
```

---

## API Error v1

Formato de error uniforme para frontend:

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

El catálogo de códigos funcionales está centralizado en `ErrorCode`.

