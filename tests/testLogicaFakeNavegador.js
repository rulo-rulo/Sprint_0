// ===================================================================================
// FICHERO: tests\testLogicaFakeNavegador.js
// DESCRIPCION: Pruebas de la logica fake del navegador
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada (navegador)
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// pruebas de la logica fake del navegador
//
// obtencion, ausencia de codigo y errores, sin usar la red
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

const fs = require('fs')
const path = require('path')
const vm = require('vm')

// ---------------------------------------------------------------
// utilidades del test
// ---------------------------------------------------------------
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
// XMLHttpRequest simulado: responde con la respuesta configurada
// ---------------------------------------------------------------
let respuestaSimulada = { status: 200, responseText: '' }

function XMLHttpRequestFalso() {
	this.readyState = 0
	this.status = 0
	this.responseText = ''
} // ()

XMLHttpRequestFalso.prototype.open = function( metodo, url, asincrono ) {
	this.metodo = metodo
	this.url = url
} // ()

XMLHttpRequestFalso.prototype.send = function() {
	this.readyState = 4
	this.status = respuestaSimulada.status
	this.responseText = respuestaSimulada.responseText
	if ( this.onreadystatechange ) {
		this.onreadystatechange()
	}
} // ()

// ---------------------------------------------------------------
// carga del fichero bajo prueba en un contexto controlado
// ---------------------------------------------------------------
const codigoFuente = fs.readFileSync(
	path.join( __dirname, '..', 'src', 'frontend_business_logic', 'obtenerUltimoCodigo.js' ), 'utf8' )

const sandbox = { XMLHttpRequest: XMLHttpRequestFalso, console: console }
vm.createContext( sandbox )
vm.runInContext( codigoFuente, sandbox )

const obtenerUltimoCodigo = sandbox.obtenerUltimoCodigo
comprobar( typeof obtenerUltimoCodigo === 'function', "el fichero define obtenerUltimoCodigo()" )

// ---------------------------------------------------------------
// obtencion de codigo
// ---------------------------------------------------------------
respuestaSimulada = {
	status: 200,
	responseText: JSON.stringify( { resultado: true, valor: 1005, fecha: "2026-10-14 17:00:00" } )
}
let capturado = {}
obtenerUltimoCodigo( function( err, res ) { capturado = { err: err, res: res } } )
comprobar(
	capturado.err === null && capturado.res !== null
		&& capturado.res.valor === 1005 && capturado.res.fecha === "2026-10-14 17:00:00",
	"obtenerUltimoCodigo devuelve valor y fecha"
)

// ---------------------------------------------------------------
// ausencia de codigo
// ---------------------------------------------------------------
respuestaSimulada = {
	status: 200,
	responseText: JSON.stringify( { resultado: false, error: "no hay ningun codigo guardado" } )
}
capturado = {}
obtenerUltimoCodigo( function( err, res ) { capturado = { err: err, res: res } } )
comprobar(
	capturado.err === "no hay ningun codigo guardado" && capturado.res === null,
	"la ausencia de codigo devuelve el error del servidor"
)

// ---------------------------------------------------------------
// fallo de red o servidor (status != 200)
// ---------------------------------------------------------------
respuestaSimulada = { status: 500, responseText: "" }
capturado = {}
obtenerUltimoCodigo( function( err, res ) { capturado = { err: err, res: res } } )
comprobar(
	capturado.err === "fallo en la peticion REST" && capturado.res === null,
	"status distinto de 200 devuelve fallo en la peticion REST"
)

// ---------------------------------------------------------------
// respuesta no interpretable
// ---------------------------------------------------------------
respuestaSimulada = { status: 200, responseText: "esto no es json" }
capturado = {}
obtenerUltimoCodigo( function( err, res ) { capturado = { err: err, res: res } } )
comprobar(
	capturado.err === "respuesta invalida" && capturado.res === null,
	"una respuesta no interpretable devuelve respuesta invalida"
)

// ---------------------------------------------------------------
// resumen
// ---------------------------------------------------------------
console.log()
if ( fallos === 0 ) {
	console.log( "[OK] todas las pruebas de la logica fake del navegador han pasado" )
	process.exit( 0 )
} else {
	console.log( "[FALLO] " + fallos + " prueba(s) han fallado" )
	process.exit( 1 )
}
