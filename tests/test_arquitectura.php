<?php

// ===================================================================================
// FICHERO: tests\test_arquitectura.php
// DESCRIPCION: Comprueba la regla de cero dependencias de la logica del negocio
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada de arquitectura
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// verifica que src/business_logic no depende del transporte
//
// la logica del negocio NO puede usar HTTP, REST, cabeceras ni los
// superglobales de peticion; la comunicacion es quien la invoca
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

$carpetaLogica      = __DIR__ . '/../src/business_logic';
$carpetaComunicacion = __DIR__ . '/../src/communication';

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
// tokens prohibidos en la logica del negocio
// (no deben aparecer ni siquiera en los comentarios)
// ---------------------------------------------------------------
$prohibidos = array(
    '$_GET', '$_POST', '$_REQUEST', '$_SERVER', '$_FILES', '$_COOKIE',
    'header(', 'setcookie(', 'http_response_code',
    'json_encode', 'json_decode',
    'curl_', 'file_get_contents',
    'communication'
);

// ---------------------------------------------------------------
// la logica del negocio no depende del transporte
// ---------------------------------------------------------------
$ficheros = glob( $carpetaLogica . '/*.php' );
comprobar( count( $ficheros ) > 0, "existen ficheros de logica del negocio" );

foreach ( $ficheros as $fichero ) {
    $nombre = basename( $fichero );
    $codigo = file_get_contents( $fichero );
    foreach ( $prohibidos as $token ) {
        comprobar(
            strpos( $codigo, $token ) === false,
            "$nombre no contiene '$token'"
        );
    }
}

// ---------------------------------------------------------------
// la comunicacion SI usa la logica del negocio (direccion correcta)
// ---------------------------------------------------------------
$ficherosComunicacion = glob( $carpetaComunicacion . '/*.php' );
comprobar( count( $ficherosComunicacion ) > 0, "existen ficheros de comunicacion" );

foreach ( $ficherosComunicacion as $fichero ) {
    $nombre = basename( $fichero );
    $codigo = file_get_contents( $fichero );
    comprobar( strpos( $codigo, 'business_logic' ) !== false,
        "communication/$nombre invoca la logica del negocio" );
}

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
echo "\n";
if ( $fallos === 0 ) {
    echo "[OK] la regla de cero dependencias se cumple\n";
    exit( 0 );
} else {
    echo "[FALLO] $fallos prueba(s) han fallado\n";
    exit( 1 );
}

?>
