<?php

// ===================================================================================
// FICHERO: src\database\conexionBD.php
// DESCRIPCION: Conexion PDO a la base de datos
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Funcion conexionBD()
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// -> conexionBD() -> conexion:Conexion | error:Texto
//
// abre una conexion PDO con la base de datos del proyecto
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

require_once( __DIR__ . '/configBD.php' );

function conexionBD() {
    $objetoResultado = new stdClass;

    try {
        $dsn = "mysql:host=" . BD_SERVIDOR . ";port=" . BD_PUERTO
             . ";dbname=" . BD_NOMBRE . ";charset=utf8mb4";

        $conexion = new PDO(
            $dsn,
            BD_USUARIO,
            BD_CLAVE,
            array( PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION )
        );

        // se fuerza el modo estricto para que faltar valor/fecha falle
        $conexion->exec( "SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'" );

        $objetoResultado->conexion = $conexion;
    } catch ( PDOException $e ) {
        $objetoResultado->error = "error al conectar con la base de datos";
    }

    return $objetoResultado;
} // ()

?>
