// ===================================================================================
// FICHERO: src\arduino\HolaMundoIBeacon\HolaMundoIBeacon.ino
// DESCRIPCION: Firmware de la placa que emite el beacon iBeacon
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Sketch principal
// ===================================================================================
// -*-c++-*-


// ============================================================================
// Autor: Raúl Sanabria Rodado
//
// Fecha:1/10/26
// ============================================================================


// ============================================================================
// ARCHIVO PRINCIPAL: HolaMundoIBeacon.ino
// Proposito: Orquestar el ciclo global de vida de la placa emisora Arduino.
// Flujo global (segun PDF Seccion 2 y 12):
//   1. setup() arranca el puerto USB, la radio BLE y el medidor.
//   2. loop() incrementa el contador, ejecuta la secuencia de LED (3.4 s),
//      obtiene la medida del medidor y publica el paquete iBeacon durante 1 s.
// ============================================================================

#include <bluefruit.h>

// Evitar colision entre las macros min/max de Arduino y la libreria estandar C++
#undef min
#undef max

#include "LED.h"
#include "PuertoSerie.h"

// Variables e instancias con alcance global de hardware
namespace Globales {
    LED elLED ( 7 );                   // LED conectado en el pin digital 7
    PuertoSerie elPuerto ( 115200 );   // Consola serie USB a 115200 baudios
};

#include "EmisoraBLE.h"
#include "Publicador.h"
#include "Medidor.h"

namespace Globales {
    Publicador elPublicador;           // Encargado de empaquetar y emitir el iBeacon
    Medidor elMedidor;                 // Origen del dato (simulado a dedo)
};

void inicializarPlaquita () {
    // Espacio reservado para configuraciones hardware de bajo nivel adicionales
}

// ============================================================================
// setup(): Inicializacion unica del sistema (PDF Seccion 2)
// ============================================================================
void setup() {
    // 1. Espera a que el puerto serie USB este listo para depurar
    Globales::elPuerto.esperarDisponible();

    // 2. Inicializa placa
    inicializarPlaquita();

    // 3. Enciende el modulo BLE
    Globales::elPublicador.encenderEmisora();

    // 4. Inicia la logica del medidor
    Globales::elMedidor.iniciarMedidor();

    // 5. Espera 1000 ms y notifica fin de arranque
    esperar( 1000 );
    Globales::elPuerto.escribir( "---- setup(): fin ---- \n " );
}

// ============================================================================
// lucecitas(): Secuencia visual de aviso (PDF Seccion 2)
//   - Bloquea la ejecucion un total de 3400 ms:
//     (100ms ON + 400ms OFF) x 3 repeticiones + (1000ms ON + 1000ms OFF) x 1
// ============================================================================
inline void lucecitas() {
    using namespace Globales;

    elLED.brillar( 100 );
    esperar ( 400 );
    elLED.brillar( 100 );
    esperar ( 400 );
    Globales::elLED.brillar( 100 );
    esperar ( 400 );
    Globales::elLED.brillar( 1000 );
    esperar ( 1000 );
}

// Variables de estado del ciclo continuo
namespace Loop {
    uint8_t cont = 0; // Contador de iteracion (0 a 255) que se empaqueta en el Major
};

// ============================================================================
// loop(): Ciclo continuo de emision (PDF Seccion 2 y 12)
// ============================================================================
void loop () {
    using namespace Loop;
    using namespace Globales;

    // 1. Incrementa el contador de iteracion
    cont++;

    elPuerto.escribir( "\n---- loop(): empieza " );
    elPuerto.escribir( cont );
    elPuerto.escribir( "\n" );

    // 2. Ejecuta el parpadeo de LED (3.4 segundos de pausa activa)
    lucecitas();

    // 3. Obtiene el dato fijado (simulado) del medidor
    int valorCO2 = elMedidor.medirCO2();

    // 4. Emite el anuncio iBeacon con el dato durante 1000 ms (1 segundo)
    elPublicador.publicarCO2( valorCO2,
                              cont,
                              1000
    );

    // 5. Cierra la iteracion por serie y repite
    elPuerto.escribir( "---- loop(): acaba **** " );
    elPuerto.escribir( cont );
    elPuerto.escribir( "\n" );
}