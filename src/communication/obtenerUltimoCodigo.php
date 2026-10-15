<?php

// ===================================================================================
// FICHERO: src\communication\obtenerUltimoCodigo.php
// DESCRIPCION: Endpoint REST que devuelve el ultimo codigo
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: API REST: obtener ultimo codigo
// ===================================================================================
require_once( __DIR__ . '/../business_logic/obtenerUltimoCodigo.php' );

// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// GET ../communication/obtenerUltimoCodigo.php
//
// @return
//  resultado:VoF
//  valor:Int, fecha:Texto   (cuando resultado=true)
//  error:Texto              (cuando resultado=false)
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

$objetoResultado = new stdClass;

$resultado = obtenerUltimoCodigo();

if ( isset( $resultado->error ) ) {
    $objetoResultado->resultado = false;
    $objetoResultado->error = $resultado->error;
} else {
    $objetoResultado->resultado = true;
    $objetoResultado->valor = $resultado->valor;
    $objetoResultado->fecha = $resultado->fecha;
}

// echo == devolver
echo json_encode( $objetoResultado );

?>
