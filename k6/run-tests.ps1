# run-tests.ps1 — Ejecutar tests de carga k6 con Docker
#
# Uso: .\k6\run-tests.ps1 [opcion]
#
# ── Tests originales ─────────────────────────────────────────────────────────
#   auth          → Load: POST /auth/login  (20 VUs)
#   search        → Load: GET /properties/search  (15 VUs)
#   stress        → Stress: GET /purchases/me  (100 VUs pico)
#
# ── Tests E2E con carga (flujos completos) ───────────────────────────────────
#   e2e-buyer     → Load E2E: flujo completo buyer  (25 VUs)
#   e2e-agency    → Load E2E: flujo completo agency (10 VUs)
#   e2e-admin     → Load E2E: flujo completo admin  (5 VUs)
#   e2e-stress    → Stress E2E: todos los roles combinados (50-80 VUs)
#
# ── Conjuntos ────────────────────────────────────────────────────────────────
#   load-all      → Ejecuta los 3 tests de carga simples
#   e2e-all       → Ejecuta los 3 flujos E2E de carga
#   stress-all    → Ejecuta stress simple + stress E2E completo
#   all           → Ejecuta TODOS los tests en orden
# ─────────────────────────────────────────────────────────────────────────────

param(
    [Parameter(Position=0)]
    [ValidateSet(
        "auth", "search", "stress",
        "e2e-buyer", "e2e-agency", "e2e-admin", "e2e-stress",
        "load-all", "e2e-all", "stress-all", "all"
    )]
    [string]$Test = "all"
)

$BASE_URL   = "http://host.docker.internal:8080"
$K6_IMAGE   = "grafana/k6"
$SCRIPTS_DIR = $PSScriptRoot

function Run-K6([string]$Script, [string]$Label) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host " Corriendo: $Label" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    $scriptPath = Join-Path $SCRIPTS_DIR $Script
    Get-Content $scriptPath | docker run --rm -i -e "BASE_URL=$BASE_URL" $K6_IMAGE run -

    if ($LASTEXITCODE -ne 0) {
        Write-Host "FALLO: $Label (exit code $LASTEXITCODE)" -ForegroundColor Red
        exit $LASTEXITCODE
    }
    Write-Host "PASO: $Label" -ForegroundColor Green
}

# Verificar que Docker esta corriendo
docker ps 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker no esta corriendo." -ForegroundColor Red
    exit 1
}

# Verificar que la API esta levantada
Write-Host "Verificando API en http://localhost:8080 ..." -ForegroundColor Yellow
try {
    $body = '{"username":"buyer1","password":"buyer123"}'
    Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $body -ContentType "application/json" | Out-Null
    Write-Host "API disponible." -ForegroundColor Green
} catch {
    Write-Host "ADVERTENCIA: La API no responde. Asegurate de correr: docker-compose up -d" -ForegroundColor Yellow
    $continuar = Read-Host "Continuar igual? (s/N)"
    if ($continuar -ne "s") { exit 1 }
}

# Ejecutar segun parametro
switch ($Test) {
    # ── Tests de carga simples (endpoints individuales) ───────────────────────
    "auth"   { Run-K6 "load-auth.js"       "Load Test - Autenticacion (20 VUs)"           }
    "search" { Run-K6 "load-search.js"     "Load Test - Busqueda de propiedades (15 VUs)" }
    "stress" { Run-K6 "stress-purchase.js" "Stress Test - Compras (100 VUs pico)"         }

    # ── Tests E2E con carga (flujos completos) ───────────────────────────────
    "e2e-buyer"  { Run-K6 "e2e-load-buyer-journey.js"  "E2E Load  - Flujo completo Buyer  (25 VUs)"          }
    "e2e-agency" { Run-K6 "e2e-load-agency-journey.js" "E2E Load  - Flujo completo Agency (10 VUs)"          }
    "e2e-admin"  { Run-K6 "e2e-load-admin-journey.js"  "E2E Load  - Flujo completo Admin  (5 VUs)"           }
    "e2e-stress" { Run-K6 "e2e-stress-full-flow.js"    "E2E Stress - Multi-rol combinado  (50-80 VUs pico)"  }

    # ── Conjuntos ─────────────────────────────────────────────────────────────
    "load-all" {
        Run-K6 "load-auth.js"   "Load Test - Autenticacion (20 VUs)"
        Run-K6 "load-search.js" "Load Test - Busqueda de propiedades (15 VUs)"
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host " Load tests PASARON                    " -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    }

    "e2e-all" {
        Run-K6 "e2e-load-buyer-journey.js"  "E2E Load - Flujo completo Buyer"
        Run-K6 "e2e-load-agency-journey.js" "E2E Load - Flujo completo Agency"
        Run-K6 "e2e-load-admin-journey.js"  "E2E Load - Flujo completo Admin"
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host " Todos los E2E load tests PASARON      " -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    }

    "stress-all" {
        Run-K6 "stress-purchase.js"      "Stress Test - Compras (100 VUs pico)"
        Run-K6 "e2e-stress-full-flow.js" "E2E Stress - Multi-rol combinado (50-80 VUs)"
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host " Todos los stress tests PASARON        " -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    }

    "all" {
        Write-Host ""
        Write-Host "======================================================" -ForegroundColor Magenta
        Write-Host " SUITE COMPLETA: Load + E2E Load + Stress             " -ForegroundColor Magenta
        Write-Host "======================================================" -ForegroundColor Magenta

        Write-Host "`n--- FASE 1: Tests de carga simples ---`n" -ForegroundColor Yellow
        Run-K6 "load-auth.js"   "Load Test - Autenticacion (20 VUs)"
        Run-K6 "load-search.js" "Load Test - Busqueda de propiedades (15 VUs)"

        Write-Host "`n--- FASE 2: Tests E2E con carga (flujos completos) ---`n" -ForegroundColor Yellow
        Run-K6 "e2e-load-buyer-journey.js"  "E2E Load - Flujo completo Buyer"
        Run-K6 "e2e-load-agency-journey.js" "E2E Load - Flujo completo Agency"
        Run-K6 "e2e-load-admin-journey.js"  "E2E Load - Flujo completo Admin"

        Write-Host "`n--- FASE 3: Tests de stress ---`n" -ForegroundColor Yellow
        Run-K6 "stress-purchase.js"      "Stress Test - Compras (100 VUs pico)"
        Run-K6 "e2e-stress-full-flow.js" "E2E Stress - Multi-rol combinado"

        Write-Host ""
        Write-Host "======================================================" -ForegroundColor Green
        Write-Host " SUITE COMPLETA: TODOS LOS TESTS PASARON              " -ForegroundColor Green
        Write-Host "======================================================" -ForegroundColor Green
    }
}

