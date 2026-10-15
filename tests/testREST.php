<?php

// ===================================================================================
// FICHERO: tests\testREST.php
// DESCRIPCION: Pruebas del API REST
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada de REST
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// pruebas de humo del servidor REST
//
// incluye los ficheros REST con $_GET configurado y captura la
// salida con ob_start(), sin usar la red
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

require_once( __DIR__ . '/../src/database/configBD.php' );

$fallos = 0;

function ok( $mensaje ) {
    echo "[OK] " . $mensaje . "\n";
} // ()

function fallo( $mensaje ) {
    global $fallos;
    $fallos++;
    echo "[FALLO] " . $mensaje . "\n";
} // ()

function comprobar( $condicion, $mensaje ) {
    if ( $condicion ) {
        ok( $mensaje );
    } else {
        fallo( $mensaje );
    }
} // ()

// ---------------------------------------------------------------
// llama a un fichero REST capturando su salida JSON
// ---------------------------------------------------------------
function llamarREST( $fichero, $parametros = array() ) {
    $_GET = $parametros;
    ob_start();
    include( __DIR__ . '/../src/communication/' . $fichero );
    $salida = ob_get_clean();
    return json_decode( $salida );
} // ()

// ---------------------------------------------------------------
// preparacion de la base de datos
// ---------------------------------------------------------------
try {
    $pdo = new PDO(
        "mysql:host=" . BD_SERVIDOR . ";port=" . BD_PUERTO,
        BD_USUARIO,
        BD_CLAVE,
        array( PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION )
    );
    $pdo->exec( "CREATE DATABASE IF NOT EXISTS " . BD_NOMBRE );
    $pdo->exec( "USE " . BD_NOMBRE );
    $pdo->exec( "SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'" );
    $pdo->exec( file_get_contents( __DIR__ . '/../src/database/esquema.sql' ) );
    ok( "base de datos preparada" );
} catch ( PDOException $e ) {
    fallo( "no se pudo preparar la base de datos: " . $e->getMessage() );
    exit( 1 );
}

// ---------------------------------------------------------------
// guardarCodigo: codigo valido
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
$respuesta = llamarREST( 'guardarCodigo.php', array( 'codigo' => '1005' ) );
comprobar( is_object( $respuesta ) && isset( $respuesta->resultado ) && $respuesta->resultado === true,
    "guardarCodigo responde resultado=true con un codigo valido" );

// ---------------------------------------------------------------
// obtenerUltimoCodigo: con datos
// ---------------------------------------------------------------
$respuesta = llamarREST( 'obtenerUltimoCodigo.php' );
comprobar(
    is_object( $respuesta ) && isset( $respuesta->resultado ) && $respuesta->resultado === true
        && isset( $respuesta->valor ) && $respuesta->valor === 1005
        && isset( $respuesta->fecha ),
    "obtenerUltimoCodigo responde resultado=true con valor y fecha"
);

// ---------------------------------------------------------------
// obtenerUltimoCodigo: sin datos
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
$respuesta = llamarREST( 'obtenerUltimoCodigo.php' );
comprobar(
    is_object( $respuesta ) && isset( $respuesta->resultado ) && $respuesta->resultado === false
        && isset( $respuesta->error ) && $respuesta->error === "no hay ningun codigo guardado",
    "obtenerUltimoCodigo responde resultado=false cuando no hay codigo"
);

// ---------------------------------------------------------------
// guardarCodigo: codigo invalido
// ---------------------------------------------------------------
$respuesta = llamarREST( 'guardarCodigo.php', array( 'codigo' => 'abc' ) );
comprobar(
    is_object( $respuesta ) && isset( $respuesta->resultado ) && $respuesta->resultado === false
        && isset( $respuesta->error ) && $respuesta->error === "codigo invalido",
    "guardarCodigo responde error=codigo invalido con un codigo no valido"
);

// ---------------------------------------------------------------
// guardarCodigo: sin parametro
// ---------------------------------------------------------------
$respuesta = llamarREST( 'guardarCodigo.php' );
comprobar(
    is_object( $respuesta ) && isset( $respuesta->resultado ) && $respuesta->resultado === false
        && isset( $respuesta->error ) && $respuesta->error === "codigo invalido",
    "guardarCodigo responde error=codigo invalido sin parametro"
);

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
echo "\n";
if ( $fallos === 0 ) {
    echo "[OK] todas las pruebas del servidor REST han pasado\n";
    exit( 0 );
} else {
    echo "[FALLO] $fallos prueba(s) han fallado\n";
    exit( 1 );
}
