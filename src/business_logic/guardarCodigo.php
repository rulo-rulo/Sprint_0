<?php

// ===================================================================================
// FICHERO: src\business_logic\guardarCodigo.php
// DESCRIPCION: Persiste el codigo en la base de datos
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Funcion guardarCodigo()
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// codigo:Texto -> guardarCodigo() -> VoF
//
// valida el codigo y, si es correcto, lo guarda en la base de datos
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

require_once( __DIR__ . '/esCodigoValido.php' );
require_once( __DIR__ . '/../database/conexionBD.php' );

function guardarCodigo( $codigo ) {

    if ( ! esCodigoValido( $codigo ) ) {
        return false;
    }

    $resultadoConexion = conexionBD();
    if ( isset( $resultadoConexion->error ) ) {
        return false;
    }

    try {
        $conexion = $resultadoConexion->conexion;

        $consulta = $conexion->prepare(
            "INSERT INTO CODIGO (valor, fecha) VALUES (:valor, :fecha)"
        );
        $consulta->execute( array(
            ':valor' => (int) $codigo,
            ':fecha' => date( 'Y-m-d H:i:s' )
        ) );

        return true;
    } catch ( PDOException $e ) {
        return false;
    }
} // ()

?>
