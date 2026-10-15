<?php

// ===================================================================================
// FICHERO: src\communication\guardarCodigo.php
// DESCRIPCION: Endpoint REST que valida y guarda un codigo
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: API REST: guardar codigo
// ===================================================================================
require_once( __DIR__ . '/../business_logic/esCodigoValido.php' );
require_once( __DIR__ . '/../business_logic/guardarCodigo.php' );

// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// GET ../communication/guardarCodigo.php?codigo=<Texto>
//
// @return
//  resultado:VoF
//  error:Texto   (solo cuando resultado=false)
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

$objetoResultado = new stdClass;

$codigo = isset( $_GET['codigo'] ) ? $_GET['codigo'] : '';

if ( ! esCodigoValido( $codigo ) ) {
    $objetoResultado->resultado = false;
    $objetoResultado->error = "codigo invalido";
} else if ( guardarCodigo( $codigo ) ) {
    $objetoResultado->resultado = true;
} else {
    $objetoResultado->resultado = false;
    $objetoResultado->error = "error al guardar el codigo";
}

// echo == devolver
echo json_encode( $objetoResultado );

?>
