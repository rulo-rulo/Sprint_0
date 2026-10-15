// ===================================================================================
// FICHERO: src\arduino\HolaMundoIBeacon\Publicador.h
// DESCRIPCION: Construye y publica el iBeacon
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Componente Publicador
// ===================================================================================
// -*- mode: c++ -*-

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO


// ============================================================================
// Autor: Raúl Sanabria Rodado
//
// Fecha:1/10/26
// ============================================================================

// ============================================================================
// CLASE: Publicador
// Proposito: Empaquetar y publicar las medidas ambientales en formato iBeacon.
// Responsabilidades (segun PDF Seccion 4 y 5):
//   - Almacenar el UUID oficial del proyecto ("EPSG-GTI-PROY-3A").
//   - Instanciar y encender la EmisoraBLE ("GTI3A-2025").
//   - Construir el campo 'major': byte alto = tipo medida, byte bajo = contador.
//   - Colocar la medida en el campo 'minor'.
//   - Controlar el tiempo de transmision del anuncio.
// ============================================================================
class Publicador {

private:
    // Identificador unico del proyecto de 16 bytes (coincide con TramalBeacon en Android)
    uint8_t beaconUUID[16] = {
            'E', 'P', 'S', 'G', '-', 'G', 'T', 'I',
            '-', 'P', 'R', 'O', 'Y', '-', '3', 'A'
    };

public:
    // EmisoraBLE configurada con el nombre del proyecto y fabricante Apple (0x004C)
    EmisoraBLE laEmisora {
            "rulitas06", // Nombre que filtra la app Android (PDF Seccion 9)
            0x004c,       // Fabricante Apple para paquetes iBeacon
            4             // Potencia de transmision (+4 dBm)
    };

    // Potencia RSSI de referencia a 1 metro (calibracion)
    const int8_t RSSI = -53; // Corregido a int8_t con signo

    // Codigos identificadores del tipo de medida (PDF Seccion 1)
    enum MedicionesID  {
        CO2 = 11,
        TEMPERATURA = 12,
        RUIDO = 13
    };

    Publicador( ) {
    }

    // Activa la radio llamando al modulo inferior
    void encenderEmisora() {
        (*this).laEmisora.encenderEmisora();
    }

    // --------------------------------------------------------------------------
    // publicarCO2(): Algoritmo de transmision de CO2 (PDF Seccion 5)
    //   1. major = (codigo_CO2 * 256) + contador
    //   2. minor = valorCO2 (el dato a dedo que recorre el sistema)
    //   3. Transmite el iBeacon durante 'tiempoEspera' ms
    //   4. Detiene el anuncio
    // --------------------------------------------------------------------------
    void publicarCO2( int16_t valorCO2, uint8_t contador,
                      long tiempoEspera ) {
        // major: byte alto = MedicionesID (11), byte bajo = contador
        uint16_t major = (static_cast<uint16_t>(MedicionesID::CO2) << 8) | contador;
        (*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID,
                                                major,
                                                valorCO2,
                                                (*this).RSSI
        );

        esperar( tiempoEspera );
        (*this).laEmisora.detenerAnuncio();
    }

    // Algoritmo equivalente para mediciones de temperatura (major con ID 12)
    void publicarTemperatura( int16_t valorTemperatura,
                              uint8_t contador, long tiempoEspera ) {
        uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;
        (*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID,
                                                major,
                                                valorTemperatura,
                                                (*this).RSSI
        );
        esperar( tiempoEspera );
        (*this).laEmisora.detenerAnuncio();
    }

};

#endif