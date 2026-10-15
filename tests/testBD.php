<?php

// ===================================================================================
// FICHERO: tests\testBD.php
// DESCRIPCION: Pruebas de la base de datos y la tabla CODIGO
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada de BD
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// pruebas de la base de datos biometria
//
// crea la base de datos y la tabla, inserta, consulta y
// comprueba las restricciones NOT NULL
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

$SERVIDOR = 'localhost';
$PUERTO   = 3306;
$USUARIO  = 'root';
$CLAVE    = '';
$BD       = 'biometria';

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
// conexion al servidor
// ---------------------------------------------------------------
try {
    $pdo = new PDO(
        "mysql:host=$SERVIDOR;port=$PUERTO",
        $USUARIO,
        $CLAVE,
        array( PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION )
    );
    ok( "conexion al servidor mysql" );
} catch ( PDOException $e ) {
    fallo( "no se pudo conectar al servidor mysql: " . $e->getMessage() );
    exit( 1 );
}

// ---------------------------------------------------------------
// creacion de la base de datos
// ---------------------------------------------------------------
try {
    $pdo->exec( "CREATE DATABASE IF NOT EXISTS $BD" );
    $pdo->exec( "USE $BD" );
    $pdo->exec( "SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'" );
    ok( "base de datos $BD creada y seleccionada" );
} catch ( PDOException $e ) {
    fallo( "no se pudo crear la base de datos: " . $e->getMessage() );
    exit( 1 );
}

// partir de cero para comprobar la creacion real de la tabla
$pdo->exec( "DROP TABLE IF EXISTS CODIGO" );

// ---------------------------------------------------------------
// aplicacion del esquema
// ---------------------------------------------------------------
try {
    $esquema = file_get_contents( __DIR__ . '/../src/database/esquema.sql' );
    $pdo->exec( $esquema );
    ok( "esquema aplicado" );
} catch ( PDOException $e ) {
    fallo( "no se pudo aplicar el esquema: " . $e->getMessage() );
    exit( 1 );
}

// ---------------------------------------------------------------
// comprobacion de la creacion
// ---------------------------------------------------------------
$existe = $pdo->query( "SHOW TABLES LIKE 'CODIGO'" )->fetchColumn();
comprobar( $existe !== false, "tabla CODIGO creada" );

$columnas = $pdo->query( "SHOW COLUMNS FROM CODIGO" )->fetchAll( PDO::FETCH_COLUMN );
comprobar(
    in_array( 'id', $columnas ) && in_array( 'valor', $columnas ) && in_array( 'fecha', $columnas ),
    "columnas id, valor y fecha presentes"
);

// ---------------------------------------------------------------
// insercion
// ---------------------------------------------------------------
try {
    $stmt = $pdo->prepare( "INSERT INTO CODIGO (valor, fecha) VALUES (:valor, :fecha)" );
    $stmt->execute( array( ':valor' => 1005, ':fecha' => date( 'Y-m-d H:i:s' ) ) );
    ok( "insercion de un codigo" );
} catch ( PDOException $e ) {
    fallo( "no se pudo insertar: " . $e->getMessage() );
}

// ---------------------------------------------------------------
// consulta
// ---------------------------------------------------------------
try {
    $fila = $pdo->query( "SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1" )
                ->fetch( PDO::FETCH_ASSOC );
    comprobar( $fila !== false && (int)$fila['valor'] === 1005, "consulta del ultimo codigo insertado" );
} catch ( PDOException $e ) {
    fallo( "no se pudo consultar: " . $e->getMessage() );
}

// ---------------------------------------------------------------
// restricciones: valor y fecha son obligatorios
// ---------------------------------------------------------------
try {
    $pdo->exec( "INSERT INTO CODIGO (fecha) VALUES (NOW())" );
    fallo( "insertar sin valor deberia fallar" );
} catch ( PDOException $e ) {
    ok( "insertar sin valor falla (restriccion NOT NULL)" );
}

try {
    $pdo->exec( "INSERT INTO CODIGO (valor) VALUES (7)" );
    fallo( "insertar sin fecha deberia fallar" );
} catch ( PDOException $e ) {
    ok( "insertar sin fecha falla (restriccion NOT NULL)" );
}

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
echo "\n";
if ( $fallos === 0 ) {
    echo "[OK] todas las pruebas de la base de datos han pasado\n";
    exit( 0 );
} else {
    echo "[FALLO] $fallos prueba(s) han fallado\n";
    exit( 1 );
}
