# ===================================================================================
# FICHERO: tests\run_all.ps1
# DESCRIPCION: Ejecuta todas las pruebas automaticas del proyecto
# COPYRIGHT: (c) 2026 Raul Sanabria Rodado
# FECHA: 2026-10-15
# AUTOR: Raul Sanabria Rodado
# APORTACION: Lanzador de pruebas
# ===================================================================================
#
# Uso:  powershell -ExecutionPolicy Bypass -File tests\run_all.ps1
#
# Requiere: php, node y javac/java en el PATH.

$proyecto = Split-Path -Parent $PSScriptRoot

$script:fallos = 0
$script:resultados = @()

function Run( $titulo, [scriptblock]$accion ) {
    Write-Host ""
    Write-Host ( "===== " + $titulo + " =====" )
    & $accion
    if ( $LASTEXITCODE -eq 0 ) {
        $script:resultados += ( "[PASA] " + $titulo )
        Write-Host ( "[PASA] " + $titulo )
    } else {
        $script:resultados += ( "[FALLA] " + $titulo )
        Write-Host ( "[FALLA] " + $titulo )
        $script:fallos++
    }
} # ()

# ---------------------------------------------------------------
# pruebas en PHP
# ---------------------------------------------------------------
Run "Base de datos" { & php ( Join-Path $proyecto "tests\testBD.php" ) }
Run "Logica del negocio" { & php ( Join-Path $proyecto "tests\testLogica.php" ) }
Run "API REST" { & php ( Join-Path $proyecto "tests\testREST.php" ) }
Run "Arquitectura" { & php ( Join-Path $proyecto "tests\test_arquitectura.php" ) }

# ---------------------------------------------------------------
# pruebas en JavaScript (Node)
# ---------------------------------------------------------------
Run "Logica fake navegador" { & node ( Join-Path $proyecto "tests\testLogicaFakeNavegador.js" ) }
Run "Interfaz UX" { & node ( Join-Path $proyecto "tests\testUx.js" ) }

# ---------------------------------------------------------------
# pruebas en Java (telefono)
# ---------------------------------------------------------------
Run "Logica fake telefono" {
    $out = Join-Path $proyecto "tests\out"
    New-Item -ItemType Directory -Force -Path $out | Out-Null
    & javac -d $out ( Join-Path $proyecto "src\frontend_business_logic\LogicaFake.java" ) ( Join-Path $proyecto "tests\TestLogicaFake.java" )
    if ( $LASTEXITCODE -eq 0 ) {
        & java -cp $out TestLogicaFake
    }
}

# ---------------------------------------------------------------
# resumen
# ---------------------------------------------------------------
Write-Host ""
Write-Host "===== RESUMEN ====="
$script:resultados | ForEach-Object { Write-Host $_ }

if ( $script:fallos -eq 0 ) {
    Write-Host "TODAS LAS PRUEBAS HAN PASADO"
    exit 0
} else {
    Write-Host ( "" + $script:fallos + " bloque(s) han fallado" )
    exit 1
}
