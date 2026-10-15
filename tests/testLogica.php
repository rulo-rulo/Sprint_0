<?php

// ===================================================================================
// FICHERO: tests\testLogica.php
// DESCRIPCION: Pruebas de la logica del negocio
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada de logica
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// pruebas de la logica de negocio
//
// valida el codigo, lo almacena, lo consulta y registra su fecha
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

require_once( __DIR__ . '/../src/database/configBD.php' );
require_once( __DIR__ . '/../src/business_logic/esCodigoValido.php' );
require_once( __DIR__ . '/../src/business_logic/guardarCodigo.php' );
require_once( __DIR__ . '/../src/business_logic/obtenerUltimoCodigo.php' );

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
// recepcion de codigo valido
// ---------------------------------------------------------------
$validos = array( "0", "42", "007", "2147483647" );
foreach ( $validos as $v ) {
    comprobar( esCodigoValido( $v ) === true, "codigo valido aceptado: \"$v\"" );
}

// ---------------------------------------------------------------
// rechazo de codigo invalido
// ---------------------------------------------------------------
$invalidos = array( "", "abc", "-5", "12a", " 12", "12345678901" );
foreach ( $invalidos as $v ) {
    comprobar( esCodigoValido( $v ) === false, "codigo invalido rechazado: \"$v\"" );
}

// ---------------------------------------------------------------
// almacenamiento correcto
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
comprobar( guardarCodigo( "1005" ) === true, "guardarCodigo acepta un codigo valido" );
comprobar( (int)$pdo->query( "SELECT COUNT(*) FROM CODIGO" )->fetchColumn() === 1, "se ha insertado una fila" );

$resultado = obtenerUltimoCodigo();
comprobar( isset( $resultado->valor ) && $resultado->valor === 1005, "obtenerUltimoCodigo devuelve el valor guardado" );

// ---------------------------------------------------------------
// consulta correcta (devuelve el ultimo)
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
guardarCodigo( "11" );
guardarCodigo( "22" );
guardarCodigo( "33" );
$resultado = obtenerUltimoCodigo();
comprobar( isset( $resultado->valor ) && $resultado->valor === 33, "obtenerUltimoCodigo devuelve el ultimo insertado" );

// ---------------------------------------------------------------
// registro de la fecha de recepcion
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
guardarCodigo( "1005" );
$resultado = obtenerUltimoCodigo();

comprobar(
    isset( $resultado->fecha ) && preg_match( '/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/', $resultado->fecha ) === 1,
    "la fecha tiene el formato Y-m-d H:i:s"
);

$momento = new DateTime( $resultado->fecha );
$ahora   = new DateTime( date( 'Y-m-d H:i:s' ) );
$diferencia = abs( $ahora->getTimestamp() - $momento->getTimestamp() );
comprobar( $diferencia <= 10, "la fecha corresponde al momento de recepcion" );

// ---------------------------------------------------------------
// caso vacio: no hay ningun codigo guardado
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
$resultado = obtenerUltimoCodigo();
comprobar(
    isset( $resultado->error ) && $resultado->error === "no hay ningun codigo guardado",
    "sin codigos guardados devuelve error"
);

// ---------------------------------------------------------------
// codigo invalido no se almacena
// ---------------------------------------------------------------
$pdo->exec( "TRUNCATE TABLE CODIGO" );
comprobar( guardarCodigo( "abc" ) === false, "guardarCodigo rechaza un codigo invalido" );
comprobar( (int)$pdo->query( "SELECT COUNT(*) FROM CODIGO" )->fetchColumn() === 0, "el codigo invalido no se ha insertado" );

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
echo "\n";
if ( $fallos === 0 ) {
    echo "[OK] todas las pruebas de la logica han pasado\n";
    exit( 0 );
} else {
    echo "[FALLO] $fallos prueba(s) han fallado\n";
    exit( 1 );
}
