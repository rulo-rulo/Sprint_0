// ===================================================================================
// FICHERO: src\frontend_business_logic\LogicaFake.java
// DESCRIPCION: Logica fake del telefono (deteccion simulada y envio REST)
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Clase LogicaFake
// ===================================================================================
// -*- mode: java -*-

// ---------------------------------------------------------------
// ---------------------------------------------------------------
//
// logica fake del telefono
//
// simula la deteccion del beacon y envia el codigo detectado por REST
//
// ---------------------------------------------------------------
// ---------------------------------------------------------------

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogicaFake {

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // callbacks y transporte inyectable
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------

    public interface CallbackEscaneo {
        void ejecutar( String error, String codigo );
    } // ()

    public interface CallbackEnvio {
        void ejecutar( String error, boolean resultado );
    } // ()

    public interface Transporte {
        void enviar( String codigo, CallbackEnvio cb );
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // TransporteREST
    //
    // transporte por defecto: envia el codigo mediante una peticion GET
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public static class TransporteREST implements Transporte {

        private String urlRest;

        public TransporteREST( String urlRest ) {
            this.urlRest = urlRest;
        } // ()

        public void enviar( String codigo, CallbackEnvio cb ) {
            try {
                URL url = java.net.URI.create( urlRest + "?codigo=" + codigo ).toURL();
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod( "GET" );

                int codigoHTTP = conexion.getResponseCode();
                if ( codigoHTTP != 200 ) {
                    cb.ejecutar( "fallo en la peticion REST", false );
                    return;
                }

                BufferedReader lector = new BufferedReader(
                        new InputStreamReader( conexion.getInputStream(), "UTF-8" ) );
                StringBuilder cuerpo = new StringBuilder();
                String linea;
                while ( ( linea = lector.readLine() ) != null ) {
                    cuerpo.append( linea );
                }
                lector.close();

                if ( cuerpo.toString().contains( "\"resultado\":true" ) ) {
                    cb.ejecutar( null, true );
                } else {
                    cb.ejecutar( "fallo en la peticion REST", false );
                }
            } catch ( Exception e ) {
                cb.ejecutar( "fallo en la peticion REST", false );
            }
        } // ()
    } // class

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // estado y configuracion
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------

    private String codigoSimulado;
    private long retrasoDeteccionMs;
    private String urlRest;
    private Transporte transporte;

    private boolean escaneando = false;
    private boolean cancelado = false;
    private String codigoDetectado = null;
    private CallbackEscaneo callbackPendiente = null;
    private Thread hiloDeteccion = null;

    public LogicaFake() {
        this( "1234", 1000, "http://localhost:8080/src/communication/guardarCodigo.php" );
    } // ()

    public LogicaFake( String codigoSimulado, long retrasoDeteccionMs, String urlRest ) {
        this.codigoSimulado = codigoSimulado;
        this.retrasoDeteccionMs = retrasoDeteccionMs;
        this.urlRest = urlRest;
        this.transporte = new TransporteREST( urlRest );
    } // ()

    public void setTransporte( Transporte transporte ) {
        this.transporte = transporte;
    } // ()

    public void setCodigoSimulado( String codigoSimulado ) {
        this.codigoSimulado = codigoSimulado;
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // -> empezarEscaneo() -> VoF
    //   (deteccion simulada; resultado via callback( error:Texto, codigo:Texto ))
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public boolean empezarEscaneo( final CallbackEscaneo cb ) {
        synchronized ( this ) {
            if ( escaneando ) {
                return false;
            }
            codigoDetectado = null;
            cancelado = false;
            escaneando = true;
            callbackPendiente = cb;

            hiloDeteccion = new Thread( new Runnable() {
                public void run() {
                    try {
                        Thread.sleep( retrasoDeteccionMs );
                    } catch ( InterruptedException e ) {
                        return;
                    }

                    CallbackEscaneo pendiente;
                    synchronized ( LogicaFake.this ) {
                        if ( cancelado ) {
                            return;
                        }
                        codigoDetectado = codigoSimulado;
                        escaneando = false;
                        pendiente = callbackPendiente;
                        callbackPendiente = null;
                        hiloDeteccion = null;
                    }

                    if ( pendiente != null ) {
                        pendiente.ejecutar( null, codigoSimulado );
                    }
                } // ()
            } );

            hiloDeteccion.start();
        }

        return true;
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // -> detenerEscaneo() -> VoF
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public boolean detenerEscaneo() {
        Thread hilo;
        CallbackEscaneo pendiente;

        synchronized ( this ) {
            if ( ! escaneando ) {
                return false;
            }
            cancelado = true;
            escaneando = false;
            codigoDetectado = null;
            hilo = hiloDeteccion;
            hiloDeteccion = null;
            pendiente = callbackPendiente;
            callbackPendiente = null;
        }

        if ( hilo != null ) {
            hilo.interrupt();
        }

        if ( pendiente != null ) {
            pendiente.ejecutar( "deteccion detenida", null );
        }

        return true;
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // -> obtenerCodigo() -> codigo:Texto | error:Texto (null si no hay codigo)
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public String obtenerCodigo() {
        synchronized ( this ) {
            return codigoDetectado;
        }
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // codigo:Texto -> enviarCodigo() -> VoF
    //   (envio via callback( error:Texto, resultado:VoF ))
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public boolean enviarCodigo( String codigo, CallbackEnvio cb ) {
        String detectado;
        synchronized ( this ) {
            detectado = codigoDetectado;
        }

        if ( ! esCodigoValido( codigo ) || detectado == null ) {
            cb.ejecutar( "codigo invalido", false );
            return false;
        }

        transporte.enviar( codigo, cb );
        return true;
    } // ()

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //
    // codigo:Texto -> esCodigoValido() -> VoF
    //
    // misma regla que la logica: solo digitos, longitud 1-10, <= 2147483647
    //
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    public static boolean esCodigoValido( String codigo ) {
        if ( codigo == null || codigo.length() == 0 ) {
            return false;
        }
        if ( codigo.length() > 10 ) {
            return false;
        }
        for ( int i = 0; i < codigo.length(); i++ ) {
            char c = codigo.charAt( i );
            if ( c < '0' || c > '9' ) {
                return false;
            }
        }
        try {
            long valor = Long.parseLong( codigo );
            return valor <= 2147483647L;
        } catch ( NumberFormatException e ) {
            return false;
        }
    } // ()

} // class
