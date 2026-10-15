// ===================================================================================
// FICHERO: tests\TestLogicaFake.java
// DESCRIPCION: Pruebas de la logica fake del telefono
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Prueba automatizada (telefono)
// ===================================================================================
// -*- mode: java -*-

// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// pruebas de la logica fake del telefono
//
// deteccion simulada, obtencion del codigo, envio y errores
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

public class TestLogicaFake {

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // utilidades del test
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------

    static int fallos = 0;

    static void comprobar( boolean condicion, String mensaje ) {
        if ( condicion ) {
            System.out.println( "[OK] " + mensaje );
        } else {
            fallos++;
            System.out.println( "[FALLO] " + mensaje );
        }
    } // ()

    static void esperar( long ms ) {
        try {
            Thread.sleep( ms );
        } catch ( InterruptedException e ) {
            // sin accion
        }
    } // ()

    // ---------------------------------------------------------------
    // captura del callback de escaneo
    // ---------------------------------------------------------------
    static class CapturaEscaneo implements LogicaFake.CallbackEscaneo {
        boolean llamado = false;
        String error;
        String codigo;

        public void ejecutar( String error, String codigo ) {
            this.llamado = true;
            this.error = error;
            this.codigo = codigo;
        } // ()
    } // class

    // ---------------------------------------------------------------
    // captura del callback de envio
    // ---------------------------------------------------------------
    static class CapturaEnvio implements LogicaFake.CallbackEnvio {
        boolean llamado = false;
        String error;
        boolean resultado;

        public void ejecutar( String error, boolean resultado ) {
            this.llamado = true;
            this.error = error;
            this.resultado = resultado;
        } // ()
    } // class

    // ---------------------------------------------------------------
    // transporte falso para no usar la red
    // ---------------------------------------------------------------
    static class TransporteFalso implements LogicaFake.Transporte {
        boolean exito;
        int veces = 0;
        String codigoRecibido = null;

        TransporteFalso( boolean exito ) {
            this.exito = exito;
        } // ()

        public void enviar( String codigo, LogicaFake.CallbackEnvio cb ) {
            veces++;
            codigoRecibido = codigo;
            if ( exito ) {
                cb.ejecutar( null, true );
            } else {
                cb.ejecutar( "fallo en la peticion REST", false );
            }
        } // ()
    } // class

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // main
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public static void main( String[] args ) {
        String url = "http://localhost:8080/src/communication/guardarCodigo.php";

        // -----------------------------------------------------------
        // deteccion simulada y obtencion del codigo
        // -----------------------------------------------------------
        LogicaFake logica = new LogicaFake( "1005", 150, url );
        TransporteFalso transporte = new TransporteFalso( true );
        logica.setTransporte( transporte );

        CapturaEscaneo captura = new CapturaEscaneo();
        comprobar( logica.empezarEscaneo( captura ) == true, "empezarEscaneo devuelve true" );
        comprobar( logica.empezarEscaneo( captura ) == false, "empezarEscaneo devuelve false si ya escanea" );
        comprobar( logica.obtenerCodigo() == null, "obtenerCodigo es null antes de detectar" );

        esperar( 400 );
        comprobar( captura.llamado == true, "el callback de escaneo se ha llamado" );
        comprobar( captura.error == null && "1005".equals( captura.codigo ), "la deteccion simulada devuelve el codigo" );
        comprobar( "1005".equals( logica.obtenerCodigo() ), "obtenerCodigo devuelve el codigo detectado" );

        // -----------------------------------------------------------
        // envio simulado con transporte falso
        // -----------------------------------------------------------
        CapturaEnvio envio = new CapturaEnvio();
        comprobar( logica.enviarCodigo( "1005", envio ) == true, "enviarCodigo devuelve true con codigo detectado y valido" );
        comprobar( transporte.veces == 1 && "1005".equals( transporte.codigoRecibido ), "el transporte recibe el codigo" );
        comprobar( envio.llamado == true && envio.error == null && envio.resultado == true, "el callback de envio confirma exito" );

        // -----------------------------------------------------------
        // codigo invalido
        // -----------------------------------------------------------
        CapturaEnvio envioInvalido = new CapturaEnvio();
        comprobar( logica.enviarCodigo( "abc", envioInvalido ) == false, "enviarCodigo devuelve false con codigo invalido" );
        comprobar( envioInvalido.llamado == true && "codigo invalido".equals( envioInvalido.error )
                && envioInvalido.resultado == false, "el callback informa codigo invalido" );

        // -----------------------------------------------------------
        // red caida (transporte que falla)
        // -----------------------------------------------------------
        LogicaFake logicaCaida = new LogicaFake( "1005", 100, url );
        logicaCaida.setTransporte( new TransporteFalso( false ) );
        CapturaEscaneo capturaCaida = new CapturaEscaneo();
        logicaCaida.empezarEscaneo( capturaCaida );
        esperar( 300 );

        CapturaEnvio envioCaida = new CapturaEnvio();
        comprobar( logicaCaida.enviarCodigo( "1005", envioCaida ) == true, "enviarCodigo delega en el transporte" );
        comprobar( envioCaida.llamado == true && "fallo en la peticion REST".equals( envioCaida.error ),
                "el callback informa fallo en la peticion REST" );

        // -----------------------------------------------------------
        // detener antes de detectar
        // -----------------------------------------------------------
        LogicaFake logicaDetener = new LogicaFake( "1005", 5000, url );
        CapturaEscaneo capturaDetener = new CapturaEscaneo();
        logicaDetener.empezarEscaneo( capturaDetener );
        comprobar( logicaDetener.detenerEscaneo() == true, "detenerEscaneo devuelve true si habia escaneo" );
        comprobar( capturaDetener.llamado == true && "deteccion detenida".equals( capturaDetener.error ),
                "el callback informa deteccion detenida" );
        comprobar( logicaDetener.obtenerCodigo() == null, "obtenerCodigo es null tras detener" );
        comprobar( logicaDetener.detenerEscaneo() == false, "detenerEscaneo devuelve false si no habia escaneo" );

        // -----------------------------------------------------------
        // enviar sin codigo detectado
        // -----------------------------------------------------------
        LogicaFake logicaVacia = new LogicaFake( "1005", 100, url );
        CapturaEnvio envioVacio = new CapturaEnvio();
        comprobar( logicaVacia.enviarCodigo( "1005", envioVacio ) == false, "enviarCodigo devuelve false sin codigo detectado" );
        comprobar( envioVacio.llamado == true && "codigo invalido".equals( envioVacio.error ),
                "el callback informa codigo invalido sin deteccion" );

        // -----------------------------------------------------------
        // resumen
        // -----------------------------------------------------------
        System.out.println();
        if ( fallos == 0 ) {
            System.out.println( "[OK] todas las pruebas de la logica fake han pasado" );
            System.exit( 0 );
        } else {
            System.out.println( "[FALLO] " + fallos + " prueba(s) han fallado" );
            System.exit( 1 );
        }
    } // main

} // class
