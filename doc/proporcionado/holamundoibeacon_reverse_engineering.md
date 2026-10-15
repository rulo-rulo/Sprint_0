# Ingeniería Inversa - HolaMundoIBeacon (Arduino)

## 1. Arquitectura del Sistema

```
+=============================================================+
|                    HARDWARE (Arduino Nano 33 BLE Sense)     |
|                                                             |
|  +-----------+    +------------+    +-------------------+   |
|  |  Sensor   |───→|  Medidor   |───→|    Publicador     |   |
|  |  CO2/Temp |    | (wrapper)  |    | (codificador)     |   |
|  +-----------+    +------------+    +--------+----------+   |
|                                              |              |
|                                              v              |
|                                     +-------------------+   |
|                                     |   EmisoraBLE      |   |
|                                     | (nRF52840 Radio)  |   |
|                                     +--------+----------+   |
|                                              |              |
|  +-----------+    +------------+             |              |
|  |   LED     |←───│  PuertoSerie│←────────────┘              |
|  | (pin 7)   |    | (115200)   │                            |
|  +-----------+    +------------+                            |
+=============================================================+
                         │
                    Radio BLE 2.4GHz
                         │
                         v
+=============================================================+
|              RECEPTOR (Smartphone / Tablet)                  |
|                                                             |
|  +-------------------+     +---------------------+          |
|  |  BluetoothLe      |────→|  Aplicación Android  |         |
|  |  Scanner          |     |  (BTLEAlumnos2021   |         |
|  +-------------------+     |   o PBIO-2025)       |         |
|                            +---------------------+          |
+=============================================================+
```

## 2. Diagrama de Clases Detallado

```
┌─────────────────────────────────────────────────────────────────┐
│                        PUBLICADOR                                │
├─────────────────────────────────────────────────────────────────┤
│ Atributos:                                                      │
│   - beaconUUID[16] : Byte  = "EPSG-GTI-PROY-3A"                │
│   - laEmisora      : EmisoraBLE  (composición)                  │
│   - RSSI           : Entero = -53                               │
├─────────────────────────────────────────────────────────────────┤
│ Enum MedicionesID:                                              │
│   CO2        = 11   (0x0B)                                     │
│   TEMPERATURA = 12   (0x0C)                                     │
│   RUIDO      = 13   (0x0D)                                     │
├─────────────────────────────────────────────────────────────────┤
│ Métodos:                                                        │
│   + encenderEmisora() : Void                                    │
│   + publicarCO2(valorCO2:Z, contador:Z, tiempoEspera:Z): Void  │
│   + publicarTemperatura(valor:Z, contador:Z, tiempo:Z): Void   │
├─────────────────────────────────────────────────────────────────┤
│ Codificación del campo MAJOR (16 bits):                         │
│                                                                 │
│   Bits [15..8]  = ID del tipo de medición (MedicionesID)        │
│   Bits [7..0]   = Contador de iteración                         │
│                                                                 │
│   Ejemplo: CO2 iteración 5 → major = (11 << 8) + 5 = 0x0B05    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       EMISORABLE                                │
├─────────────────────────────────────────────────────────────────┤
│ Atributos:                                                      │
│   - nombreEmisora : Texto = "GTI3A-2025"                       │
│   - fabricanteID  : Entero = 0x004C (Apple)                     │
│   - txPower       : Entero = 4                                  │
│   - emisor        : BLEDevice*                                  │
│   - servidor      : BLEServer*                                  │
│   - servicio      : BLEService*                                 │
├─────────────────────────────────────────────────────────────────┤
│ Métodos:                                                        │
│   + EmisoraBLE(nombre, fabID, txPower) : EmisoraBLE             │
│   + encenderEmisora() : Void                                    │
│   + detenerAnuncio() : Void                                     │
│   + emitirAnuncioIBeacon(uuid, major, minor, rssi) : Void       │
│   + emitirAnuncioIBeaconLibre(datos, longitud) : Void           │
│   + anyadirServicio(servicio) : Void                            │
│   + getConexion() : BLEDevice*                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     SERVICIOENEMISORA                            │
├─────────────────────────────────────────────────────────────────┤
│ Atributos:                                                      │
│   - servicio     : BLEService*                                  │
│   - emisora      : EmisoraBLE*                                  │
│   - caracteristica : BLECharacteristic*                         │
├─────────────────────────────────────────────────────────────────┤
│ Métodos:                                                        │
│   + ServicioEnEmisora(uuid, emisora) : ServicioEnEmisora        │
│   + anyadirCaracteristica(c) : Void                             │
│   + getServicio() : BLEService*                                 │
└─────────────────────────────────────────────────────────────────┘

┌───────────────────────────────┐   ┌───────────────────────────┐
│          MEDIDOR              │   │           LED             │
├───────────────────────────────┤   ├───────────────────────────┤
│ (sin atributos)               │   │ - pin : Entero            │
├───────────────────────────────┤   ├───────────────────────────┤
│ + iniciarMedidor() : Void     │   │ + LED(pin) : LED          │
│ + medirCO2() : Entero         │   │ + brillar(tiempo) : Void  │
│ + medirTemperatura() : Entero │   └───────────────────────────┘
└───────────────────────────────┘

┌───────────────────────────────┐
│        PUERTOSERIE            │
├───────────────────────────────┤
│ - velocidad : Entero          │
├───────────────────────────────┤
│ + PuertoSerie(vel) : Puerto.. │
│ + esperarDisponible() : Void  │
│ + escribir(dato) : Void       │
└───────────────────────────────┘
```

## 3. Diagrama de Secuencia - Flujo Completo

```
Temporizador          setup()              loop()
    │                   │                    │
    │    ┌──────────────┤                    │
    │    │  INICIO      │                    │
    │    │              │                    │
    │    │  elPuerto    │                    │
    │    │  .esperar    │                    │
    │    │  Disponible()│                    │
    │    │              │                    │
    │    │  inicializar │                    │
    │    │  Plaquita()  │                    │
    │    │              │                    │
    │    │  elPublicador│                    │
    │    │  .encender   │                    │
    │    │  Emisora()   │                    │
    │    │     │        │                    │
    │    │     └──→ EmisoraBLE               │
    │    │          .encenderEmisora()       │
    │    │              │                    │
    │    │  elMedidor   │                    │
    │    │  .iniciar    │                    │
    │    │  Medidor()   │                    │
    │    │              │                    │
    │    │  esperar(1000)│                   │
    │    │              │                    │
    │    └──────────────┤                    │
    │                   │    ┌───────────────┤
    │                   │    │  CONTENIDO    │
    │                   │    │  LOOP()       │
    │                   │    │               │
    │                   │    │  cont++       │
    │                   │    │               │
    │                   │    │  lucecitas()  │
    │                   │    │   ├── LED     │
    │                   │    │   │  .brillar │
    │                   │    │   └── esperar │
    │                   │    │               │
    │                   │    │  valorCO2 =   │
    │                   │    │  elMedidor    │
    │                   │    │  .medirCO2()  │
    │                   │    │     │         │
    │                   │    │     └──→ return│
    │                   │    │        123    │
    │                   │    │               │
    │                   │    │  elPublicador │
    │                   │    │  .publicarCO2 │
    │                   │    │  (123,cont,   │
    │                   │    │   1000)       │
    │                   │    │     │         │
    │                   │    │     ▼         │
    │                   │    │  ┌───────────┐│
    │                   │    │  │ MAJOR =   ││
    │                   │    │  │ (11<<8)+  ││
    │                   │    │  │ cont      ││
    │                   │    │  └─────┬─────┘│
    │                   │    │        │      │
    │                   │    │        ▼      │
    │                   │    │  ┌───────────┐│
    │                   │    │  │ emitir    ││
    │                   │    │  │ iBeacon   ││
    │                   │    │  │ (UUID,    ││
    │                   │    │  │  major,   ││
    │                   │    │  │  minor,   ││
    │                   │    │  │  txPower) ││
    │                   │    │  └─────┬─────┘│
    │                   │    │        │      │
    │                   │    │        ▼      │
    │                   │    │  ═════════════│
    │                   │    │  ║ EMISION   ║│
    │                   │    │  ║ BLE RADIO ║│
    │                   │    │  ═════════════│
    │                   │    │        │      │
    │                   │    │  esperar(1000)│
    │                   │    │        │      │
    │                   │    │  detenerAnuncio│
    │                   │    │  ()           │
    │                   │    │               │
    │                   │    └───────────────┤
    │                   │         │          │
    │                   │         └── loop() │
    │                   │             │      │
```

## 4. Formato del Paquete iBeacon (30 bytes)

```
Byte:   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
        │ 02 01 06 1a ff 4c 00 02 15 │ UUID (16 bytes)    │
        ├─────────────────────────────┼────────────────────┤
        │ advFlags  │advH│compID│type│                     │
        │  (3B)     │(2B) │(2B)  │(1B│                     │
                                              (continua...)

Byte:  16 17 18 19 20 21 22 23 24 25 26 27 28 29
        ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
        │ UUID (cont.)     │ Major  │Minor │TX │
        │                  │ (2B)   │(2B)  │(1B)│
        └──────────────────┴────────┴──────┴────┘
```

## 5. Tabla de Tipos de Datos

| Tipo     | Dominio        | Uso                                   |
|----------|----------------|---------------------------------------|
| Z        | Entero         | CO2, temperatura, RSSI, contador      |
| B        | Byte           | UUID, campos BLE, trama iBeacon       |
| Texto    | String         | Nombre de emisora, mensajes de log    |
| N        | Natural        | Contador, tiempos de espera           |
| R        | Real           | (no usado actualmente)                |
| B        | Booleano       | (no usado actualmente)                |

## 6. Protocolo de Comunicación

```
Arduino (EMISOR)                          Smartphone (RECEPTOR)
     │                                           │
     │  1. advertising(iBeacon)                  │
     │  ─────────────────────────────────────→    │
     │     [UUID: EPSG-GTI-PROY-3A]              │
     │     [Major: (tipo<<8)+cont]                │
     │     [Minor: valorCO2]                      │
     │     [TX Power: 4]                          │
     │                                           │
     │  2. advertising(iBeacon)                  │
     │  ─────────────────────────────────────→    │  Escaneo BLE
     │     [UUID: EPSG-GTI-PROY-3A]              │  ↓
     │     [Major: (tipo<<8)+cont+1]              │  ScanResult
     │     [Minor: valorCO2]                      │  ↓
     │     [TX Power: 4]                          │  TramaIBeacon()
     │                                           │  ↓
     │  3. stopAdvertising                       │  mostrarInfo()
     │  ←─────────────────────────────────────    │  ↓
     │                                           │  Logcat: ">>>> uuid=..."
```
