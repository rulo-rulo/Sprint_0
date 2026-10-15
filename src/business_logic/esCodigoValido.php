<?php

// ===================================================================================
// FICHERO: src\business_logic\esCodigoValido.php
// DESCRIPCION: Valida que el codigo recibido es un entero admisible
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Funcion de validacion del codigo
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// codigo:Texto -> esCodigoValido() -> VoF
//
// comprueba que el codigo recibido es un entero valido para la base de datos
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

function esCodigoValido( $codigo ) {

    if ( ! is_string( $codigo ) ) {
        return false;
    }

    if ( $codigo === '' ) {
        return false;
    }

    if ( ! ctype_digit( $codigo ) ) {
        return false;
    }

    $longitud = strlen( $codigo );
    if ( $longitud < 1 || $longitud > 10 ) {
        return false;
    }

    if ( (int) $codigo > 2147483647 ) {
        return false;
    }

    return true;
} // ()

?>
