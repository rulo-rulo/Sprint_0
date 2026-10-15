<?php

// ===================================================================================
// FICHERO: src\database\configBD.sample.php
// DESCRIPCION: Plantilla de configuracion de la base de datos
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Constantes de conexion
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// PLANTILLA de configuracion de la base de datos
//
// Copia este fichero como configBD.php y rellena los valores reales.
// configBD.php NO se sube al repositorio (esta en .gitignore).
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

date_default_timezone_set( 'Europe/Madrid' );

define( 'BD_SERVIDOR', 'localhost' );
define( 'BD_PUERTO',   3306 );
define( 'BD_USUARIO',  '<usuario>' );
define( 'BD_CLAVE',    '<clave>' );
define( 'BD_NOMBRE',   'biometria' );

?>
