// ===================================================================================
// FICHERO: src\arduino\HolaMundoIBeacon\Medidor.h
// DESCRIPCION: Proporciona la medida ficticia (medirCO2)
// COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
// FECHA: 2026-10-15
// AUTOR: Raúl Sanabria Rodado
// APORTACION: Componente Medidor
// ===================================================================================
// -*- mode: c++ -*-

#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ============================================================================
// Autor: Raúl Sanabria Rodado
//
// Fecha:1/10/26
// ============================================================================

// ============================================================================
// CLASE: Medidor
// Proposito: Simular o encapsular la adquisicion de datos ambientales.
// Responsabilidades (segun PDF Seccion 4):
//   - No posee estado interno.
//   - Proporciona valores de medida simulados "a dedo" para las pruebas.
//   - En este sprint no lee sensores fisicos, devuelve valores fijos.
// ============================================================================
class Medidor {

private:

public:
    // Constructor por defecto
    Medidor(  ) {
    }

    // Punto de entrada para inicializar buses I2C/SPI si hubiera sensor fisico
    void iniciarMedidor() {
    }

    // Devuelve la medicion simulada de CO2.
    int medirCO2() {
        return 1005; //medicion simulada
    }

    // Devuelve la medicion simulada de Temperatura.
    int medirTemperatura() {
        return -12;
    }

};

#endif