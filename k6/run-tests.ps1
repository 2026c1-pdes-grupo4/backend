# run-tests.ps1 — Ejecutar tests de carga k6 con Docker
# Uso: .\k6\run-tests.ps1 [auth|search|stress|all]

param(
    [Parameter(Position=0)]
    [ValidateSet("auth", "search", "stress", "all")]
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
    "auth"   { Run-K6 "load-auth.js"       "Load Test - Autenticacion (20 VUs)"         }
    "search" { Run-K6 "load-search.js"     "Load Test - Busqueda de propiedades (15 VUs)" }
    "stress" { Run-K6 "stress-purchase.js" "Stress Test - Compras (100 VUs)"             }
    "all" {
        Run-K6 "load-auth.js"       "Load Test - Autenticacion (20 VUs)"
        Run-K6 "load-search.js"     "Load Test - Busqueda de propiedades (15 VUs)"
        Run-K6 "stress-purchase.js" "Stress Test - Compras (100 VUs)"
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host " Todos los tests de carga PASARON      " -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    }
}

