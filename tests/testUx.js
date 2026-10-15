// ===================================================================================
// FICHERO: tests\testUx.js
// DESCRIPCION: Pruebas de la interfaz web
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada de UX
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// prueba estructural ligera de la interfaz web
//
// comprueba sin navegador que Aplicacion.html tiene las piezas clave
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

const fs = require('fs')
const path = require('path')

let fallos = 0

function comprobar( condicion, mensaje ) {
	if ( condicion ) {
		console.log( "[OK] " + mensaje )
	} else {
		fallos++
		console.log( "[FALLO] " + mensaje )
	}
} // ()

// ---------------------------------------------------------------
// lectura del fichero
// ---------------------------------------------------------------
const ruta = path.join( __dirname, '..', 'src', 'gui', 'Aplicacion.html' )
let html = ""
try {
	html = fs.readFileSync( ruta, 'utf8' )
	comprobar( true, "Aplicacion.html se puede leer" )
} catch ( e ) {
	comprobar( false, "Aplicacion.html se puede leer" )
	console.log( "\n[FALLO] no se pudo leer Aplicacion.html" )
	process.exit( 1 )
}

// ---------------------------------------------------------------
// comprobaciones estructurales
// ---------------------------------------------------------------
comprobar( /<!DOCTYPE html>/i.test( html ), "declara el tipo de documento HTML" )
comprobar( /<html[\s>]/i.test( html ) && /<\/html>/i.test( html ), "contiene la etiqueta html" )
comprobar( /<body[\s>]/i.test( html ) && /<\/body>/i.test( html ), "contiene la etiqueta body" )
comprobar( /charset\s*=\s*["']?utf-8/i.test( html ), "declara la codificacion utf-8" )

comprobar(
	/script[^>]*src\s*=\s*["']\.\.\/frontend_business_logic\/obtenerUltimoCodigo\.js["']/i.test( html ),
	"incluye la logica fake obtenerUltimoCodigo.js"
)

comprobar(
	/id\s*=\s*["']codigo["']/i.test( html ),
	"tiene un elemento con id=codigo"
)

comprobar(
	/obtenerUltimoCodigo\s*\(/.test( html ),
	"llama a la funcion obtenerUltimoCodigo()"
)

comprobar(
	/onload/i.test( html ),
	"consulta el codigo al cargar la pagina"
)

comprobar(
	/\.innerHTML\s*=/.test( html ),
	"escribe el resultado en la pagina"
)

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
console.log()
if ( fallos === 0 ) {
	console.log( "[OK] todas las pruebas estructurales de la interfaz han pasado" )
	process.exit( 0 )
} else {
	console.log( "[FALLO] " + fallos + " prueba(s) han fallado" )
	process.exit( 1 )
}
