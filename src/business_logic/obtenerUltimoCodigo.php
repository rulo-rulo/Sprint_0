<?php

// ===================================================================================
// FICHERO: src\business_logic\obtenerUltimoCodigo.php
// DESCRIPCION: Recupera el ultimo codigo almacenado
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Funcion obtenerUltimoCodigo()
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// -> obtenerUltimoCodigo() -> (valor:Int, fecha:Texto) | error:Texto
//
// devuelve el ultimo codigo guardado o un error si no hay ninguno
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

require_once( __DIR__ . '/../database/conexionBD.php' );

function obtenerUltimoCodigo() {
    $objetoResultado = new stdClass;

    $resultadoConexion = conexionBD();
    if ( isset( $resultadoConexion->error ) ) {
        $objetoResultado->error = $resultadoConexion->error;
        return $objetoResultado;
    }

    try {
        $conexion = $resultadoConexion->conexion;

        $consulta = $conexion->prepare(
            "SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1"
        );
        $consulta->execute();

        $fila = $consulta->fetch( PDO::FETCH_ASSOC );

        if ( $fila === false ) {
            $objetoResultado->error = "no hay ningun codigo guardado";
        } else {
            $objetoResultado->valor = (int) $fila['valor'];
            $objetoResultado->fecha = $fila['fecha'];
        }
    } catch ( PDOException $e ) {
        $objetoResultado->error = "error al consultar la base de datos";
    }

    return $objetoResultado;
} // ()

?>
