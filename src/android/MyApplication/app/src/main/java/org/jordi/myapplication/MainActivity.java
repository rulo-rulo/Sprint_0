// ===================================================================================
// FICHERO: src\android\MyApplication\app\src\main\java\org\jordi\myapplication\MainActivity.java
// DESCRIPCION: Actividad que escanea beacons y envia el codigo por REST
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Pantalla principal de la app
// ===================================================================================
package org.jordi.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;

import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

// ===================================================================================
// CLASE: MainActivity
// Proposito: escanear beacons BTLE e imprimir por Logcat la trama iBeacon, incluido
//            el "minor" (el codigo 1005) que emite el beacon.
// ===================================================================================
public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // --------------------------------------------------------------
    // configuracion del envio al servidor REST
    //
    // El emisor esta desplegado en Plesk (UPV), que es donde el proyecto
    // empezo a funcionar. Cuando toque migrar a XAMPP basta con cambiar
    // esta unica linea por la direccion local del PC que hace de servidor,
    // del estilo de http://172.31.151.124/Sprint_0/src/communication/...
    // No hay ninguna otra direccion escondida en el codigo.
    // --------------------------------------------------------------
    private static final String NOMBRE_EMISORA = "rulitas06";
    private static final String URL_REST = "https://rsanrod1.upv.edu.es/Sprint_0/src/communication/guardarCodigo.php";

    // Cada vuelta del loop() dura unos 4400 ms (3400 de lucecitas + 1000
    // anunciando). Con un intervalo mayor que eso, como los 5000 ms que
    // habia, el beacon transmitia dos veces por vuelta pero solo se enviaba
    // una de cada dos, y en pantalla llegaba la mitad de veces.
    private static final long INTERVALO_ENVIO_MS = 4000;

    private long ultimoEnvioMs = 0;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    // Arranque automatico del escaneo 1 s despues de conceder permisos.
    //
    // Se guarda la referencia al Runnable para poder CANCELARLO desde el
    // boton de detener. Sin esto, si se pulsa "Detener" antes de que
    // transcurra ese segundo, el runnable llega igualmente, arranca el
    // escaneo por su cuenta y la app vuelve a escanear justo despues de
    // haberla parado, que era otro de los sintomas del boton que "no
    // funcionaba".
    // --------------------------------------------------------------
    private final Handler elManejador = new Handler(Looper.getMainLooper());

    private Runnable arranqueAutomatico = null;

    // --------------------------------------------------------------
    // obtenerEscanner(): devuelve el escaner BTLE listo para usar.
    //
    // Antes se guardaba una unica vez en inicializarBlueTooth(), pero
    // getBluetoothLeScanner() devuelve null mientras el adaptador esta
    // apagado y bta.enable() es asincrono: al arrancar la app el escaner
    // aun no existe y cualquier pulsacion del boton estallaba en NPE.
    // Se vuelve a pedir en cada arranque de escaneo.
    // --------------------------------------------------------------
    private BluetoothLeScanner obtenerEscanner() {
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        if (bta == null) {
            Log.d(ETIQUETA_LOG, " obtenerEscanner(): este telefono no tiene BTLE");
            return null;
        }

        if (!bta.isEnabled()) {
            // requiere BLUETOOTH_CONNECT; si no esta concedido no hace nada
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                bta.enable();
            }
            Log.d(ETIQUETA_LOG, " obtenerEscanner(): adaptador apagado, aun no se puede escanear");
            return null;
        }

        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, " obtenerEscanner(): no hay escaner disponible aun");
        }

        return this.elEscanner;
    } // ()

    // --------------------------------------------------------------
    // nombreLocalDelRegistro(): lee el nombre local (AD type 0x09) del
    // registro de escaneo, recorriendo las estructuras de datos de anuncio.
    //
    // NO se usa BluetoothDevice.getName(): ese metodo exige el permiso
    // BLUETOOTH_CONNECT y en terminales con MIUI devuelve null para los
    // dispositivos con los que no hay conexion establecida. Si el filtro
    // dependiera de eso, el beacon dejaria de reconocerse.
    //
    // El nombre viaja en el paquete de respuesta al escaneo, asi que se
    // lee de ahi directamente, sin pedir ningun permiso.
    // --------------------------------------------------------------
    private String nombreLocalDelRegistro(byte[] bytes) {
        int i = 0;
        while (i + 1 < bytes.length) {
            int longitud = bytes[i] & 0xFF;   // incluye el byte de tipo
            if (longitud == 0) {
                break;                        // fin de las estructuras
            }
            int tipo = bytes[i + 1] & 0xFF;
            if (tipo == 0x09 && longitud >= 2 && i + 1 + longitud <= bytes.length) {
                return new String(bytes, i + 2, longitud - 1);
            }
            i += longitud + 1;                // salta a la estructura siguiente
        }
        return null;
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        // getScanRecord() devuelve null mientras el sistema aun no ha
        // reconstruido el registro; sin esta comprobacion la app se cae
        android.bluetooth.le.ScanRecord registro = resultado.getScanRecord();
        if (registro == null) {
            return;
        }

        byte[] bytes = registro.getBytes();
        if (bytes == null || bytes.length < 30) {
            // una trama de menos de 30 bytes no puede ser un iBeacon
            return;
        }

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        int rssi = resultado.getRssi();

        // el nombre se lee del paquete, no del dispositivo
        String nombre = nombreLocalDelRegistro(bytes);

        // el filtro de nombre no es cosmético: en el aula todos los Arduino
        // llevan la misma plantilla y por tanto el mismo UUID, de modo que
        // el nombre es lo unico que distingue esta emisora de la de un compañero
        // ATENCION: aqui no se imprime nada. El callback de escaneo se dispara
        // con TODOS los beacon BTLE del aula, y si cada descartado dejara su
        // linea el logcat se volvia inutilizable (decenas de lineas por segundo
        // de beacons que no son el nuestro). Se vuelve en silencio.
        if (!NOMBRE_EMISORA.equals(nombre)) {
            return;
        }

        TramaIBeacon tib = new TramaIBeacon(bytes);
        int codigo = Utilidades.bytesToInt(tib.getMinor());

        // volcado completo, pero SOLO de la emisora que nos interesa: si se
        // imprimiera de todos los dispositivos del aula el logcat se inundaria
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** EMISORA " + NOMBRE_EMISORA + " DETECTADA ******");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre    = " + nombre);
        Log.d(ETIQUETA_LOG, " toString  = " + bluetoothDevice.toString());
        Log.d(ETIQUETA_LOG, " direccion = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi      = " + rssi);
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));
        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength())
                + " ( " + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + codigo + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        enviarCodigoDetectado(codigo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        // ------------------------------------------------------------------
        // ATENCION: primero se para lo que hubiera. Este metodo creaba un
        // ScanCallback nuevo y lo registraba con startScan() POR ENCIMA del
        // anterior sin detenerlo, asi que cada pulsacion dejaba un escaneo
        // huerfano corriendo en el stack de Bluetooth. El boton de detener
        // solo llamaba a stopScan() sobre el ultimo (callbackDelEscaneo), de
        // modo que paraba uno y los demas seguian escaneando y enviando
        // codigos al servidor. Con dos o tres pulsaciones ya era imposible
        // parar la app de verdad.
        //
        // Garantizando que solo hay un escaneo activo como mucho, detener()
        // siempre alcanza al que esta corriendo.
        // ------------------------------------------------------------------
        detenerBusquedaDispositivosBTLE();

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): buscando " + dispositivoBuscado);

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);

                // el filtro por nombre vive ya dentro de
                // mostrarInformacionDispositivoBTLE(), que lo lee del paquete de
                // respuesta al escaneo. Antes se usaba aqui getDevice().getName(),
                // que exige BLUETOOTH_CONNECT y devuelve null en MIUI, con lo que
                // este boton no encontraba nunca la emisora.
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        BluetoothLeScanner escanner = obtenerEscanner();
        if (escanner == null) {
            Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): sin escaner, no se empieza");
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empezar scan");

        escanner.startScan(this.callbackDelEscaneo);

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): termina");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): empieza");

        // ------------------------------------------------------------------
        // Se cancela el arranque automatico PRIMERO y antes de cualquier
        // return temprano. Si se pulsaba "Detener" durante el segundo de
        // espera, callbackDelEscaneo todavia era null y el metodo salia por
        // el if de abajo sin tocar nada, pero el runnable pendiente seguia
        // en la cola del Handler: llegaba poco despues y arrancaba el escaneo
        // igualmente. El resultado visible era que la app se paraba y a los
        // dos segundos volvia a escanear sola.
        // ------------------------------------------------------------------
        if (this.arranqueAutomatico != null) {
            this.elManejador.removeCallbacks(this.arranqueAutomatico);
            this.arranqueAutomatico = null;
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): arranque automatico cancelado");
        }

        if (this.callbackDelEscaneo == null) {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): no habia escaneo activo, termina");
            return;
        }

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): parando scan");

        // el callback se guarda en una variable local porque al nullear el
        // campo de la clase no queda referencia con la que llamar a stopScan()
        ScanCallback elCallback = this.callbackDelEscaneo;
        this.callbackDelEscaneo = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): sin permiso BLUETOOTH_SCAN, no se puede parar");
            return;
        }

        // el escaner puede no existir si el adaptador se apago mientras tanto
        if (this.elEscanner != null) {
            this.elEscanner.stopScan(elCallback);
        } else {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): no hay escaner, no hay escaneo que parar");
        }

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): termina");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        // el nombre debe coincidir con el de la emisora (Publicador.h -> setLocalName)
        this.buscarEsteDispositivoBTLE("rulitas06");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): empieza ");

        // ------------------------------------------------------------------
        // Los permisos se piden PRIMERO, nada mas entrar.
        //
        // Antes habia aqui un "if (no tengo BLUETOOTH_CONNECT) return;" puesto
        // por encima de requestPermissions(). Como en un primer arranque todavia
        // no se ha concedido ese permiso, el metodo se salia por ahi y la
        // peticion de permisos NUNCA llegaba a lanzarse: la app quedaba muda,
        // sin escanear y sin ningun mensaje en pantalla que lo explicara.
        // ------------------------------------------------------------------
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): pidiendo permisos ");

        requestPermissions(
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                CODIGO_PETICION_PERMISOS);

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): permisos pedidos");

        // de aqui en adelante todo lo que toca el adaptador es lo mejor que
        // se pueda: el escaner no se guarda aqui, se pide en obtenerEscanner()
        // cuando realmente hace falta y ya se puede usar
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        if (bta == null) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): este telefono no tiene BTLE");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            bta.enable();
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): adaptador habilitado = " + bta.isEnabled());
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): TERMINA ");
    } // ()

    // --------------------------------------------------------------
    // envio automatico del codigo detectado al REST (con throttle)
    // --------------------------------------------------------------
    private void enviarCodigoDetectado(final int codigo) {
        if (codigo < 0) {
            Log.d(ETIQUETA_LOG, " codigo no valido, no se envia: " + codigo);
            return;
        }

        long ahora = System.currentTimeMillis();
        if (ahora - this.ultimoEnvioMs < INTERVALO_ENVIO_MS) {
            Log.d(ETIQUETA_LOG, " throttle: aun no toca enviar (" + (ahora - this.ultimoEnvioMs)
                    + " ms desde el ultimo envio)");
            return;
        }
        this.ultimoEnvioMs = ahora;

        Log.d(ETIQUETA_LOG, " enviarCodigoREST(): enviando codigo " + codigo + " a " + URL_REST);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conexion = null;
                try {
                    URL url = new URL(URL_REST + "?codigo=" + codigo);
                    conexion = (HttpURLConnection) url.openConnection();
                    conexion.setRequestMethod("GET");
                    conexion.setConnectTimeout(5000);
                    conexion.setReadTimeout(5000);

                    int codigoHTTP = conexion.getResponseCode();

                    BufferedReader lector = new BufferedReader(
                            new InputStreamReader(conexion.getInputStream(), "UTF-8"));
                    StringBuilder cuerpo = new StringBuilder();
                    String linea;
                    while ((linea = lector.readLine()) != null) {
                        cuerpo.append(linea);
                    }
                    lector.close();

                    Log.d(ETIQUETA_LOG, " REST respuesta (" + codigoHTTP + "): " + cuerpo.toString());
                } catch (Exception e) {
                    Log.d(ETIQUETA_LOG, " fallo en la peticion REST: " + e.getMessage());
                } finally {
                    if (conexion != null) {
                        conexion.disconnect();
                    }
                }
            }
        }).start();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): EMPIEZA ");

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // ATENCION: no se mira grantResults[0]. Esa posicion es
                // BLUETOOTH, que en el manifiesto lleva maxSdkVersion=30 y por
                // tanto en Android 12 o superior se resuelve SIEMPRE como
                // DENEGADO aunque el usuario acepte todo. Comprobando ese
                // vector la app creia que no tenia permisos y el arranque
                // automatico del escaneo nunca llegaba a ejecutarse.
                //
                // Lo que de verdad hace falta para escanear es BLUETOOTH_SCAN,
                // que es el que se pide en la posicion 2 del array.
                boolean sePuedeEscanear = ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

                if (sePuedeEscanear) {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permiso de escaneo concedido, se empieza solo");

                    // se espera un segundo porque enable() es asincrono y el
                    // escaner todavia puede no estar disponible. El runnable se
                    // guarda en un campo en vez de crearse y olvidarse: si no,
                    // el boton de detener no puede cancelarlo y si el usuario lo
                    // pulsa dentro de ese segundo la app vuelve a escanear sola.
                    this.arranqueAutomatico = new Runnable() {
                        @Override
                        public void run() {
                            arranqueAutomatico = null;
                            buscarEsteDispositivoBTLE(NOMBRE_EMISORA);
                        }
                    };
                    this.elManejador.postDelayed(this.arranqueAutomatico, 1000);
                } else {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): sin permiso BLUETOOTH_SCAN, no se puede escanear");
                }
                return;
        }
    } // ()

    // --------------------------------------------------------------
    // sin esto la app puede dejar el escaneo corriendo tras cerrarse
    // --------------------------------------------------------------
    @Override
    protected void onDestroy() {
        Log.d(ETIQUETA_LOG, " onDestroy(): parando el escaneo");
        detenerBusquedaDispositivosBTLE();
        super.onDestroy();
    } // ()

} // class
