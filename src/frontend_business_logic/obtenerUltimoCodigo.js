// ===================================================================================
// FICHERO: src\frontend_business_logic\obtenerUltimoCodigo.js
// DESCRIPCION: Logica fake del navegador que consulta el ultimo codigo
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Funcion obtenerUltimoCodigo() (navegador)
// ===================================================================================
// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// versión fake de una función de la lógica
//
// -> obtenerUltimoCodigo() -> (valor:Int, fecha:Texto) | error:Texto
//
// (devuelto via callback( error:Texto, resultado ))
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------
function obtenerUltimoCodigo( cb ) {

	// preparar la llamada remota
	var xmlhttp = new XMLHttpRequest()

	xmlhttp.onreadystatechange = function() {

		if ( this.readyState != 4 ) {
			return
		}

		// fallo de red o de servidor
		if ( this.status != 200 ) {
			cb( "fallo en la peticion REST", null )
			return
		}

		var respuesta
		try {
			respuesta = JSON.parse( this.responseText )
		} catch ( e ) {
			cb( "respuesta invalida", null )
			return
		}

		if ( respuesta.resultado === true ) {
			cb( null, { valor: respuesta.valor, fecha: respuesta.fecha } )
		} else {
			// ausencia de código: el servidor informa del error
			cb( respuesta.error, null )
		}
	}

	// llamamos *remotamente* al fichero obtenerUltimoCodigo.php
	// (la verdadera función de la lógica)
	xmlhttp.open( "GET", "../communication/obtenerUltimoCodigo.php", true )
	xmlhttp.send()

} // ()
