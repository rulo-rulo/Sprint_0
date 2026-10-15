# Ingeniería Inversa - PBIO-2025-Sprint0 (Sistema Completo)

## 1. Arquitectura General del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SISTEMA IoT - FLUJO COMPLETO                         │
│                                                                         │
│  FASE 1: SENSOR        FASE 2: EMISSOR       FASE 3: RECEPTOR          │
│  ┌──────────┐          ┌──────────────┐       ┌──────────────────┐      │
│  │ SENSOR   │    ┌────→│  EMISORBLE   │──────→│  BLUETOOTHLE     │      │
│  │ CO2/TEMP │────┘     │  (Arduino)   │ Radio │  SCANNER         │      │
│  └──────────┘          └──────────────┘  BLE  │  (Android)       │      │
│       │                      │                └────────┬─────────┘      │
│       v                      v                         v                │
│  ┌──────────┐          ┌──────────────┐       ┌──────────────────┐      │
│  │ MEDIDOR  │          │ PUBLICADOR   │       │ TRAMAIBeacon     │      │
│  │ (wrapper)│          │ (codificador)│       │ (parseador)      │      │
│  └──────────┘          └──────────────┘       └────────┬─────────┘      │
│                                                        v                │
│                                                ┌──────────────────┐      │
│  FASE 4: LOGICA          FASE 5: UI            │ MAINACTIVITY     │      │
│  ┌──────────────┐       ┌──────────────┐       │ (escaneo+log)    │      │
│  │  PHP/REST    │←──────│  JAVASCRIPT  │←──────│ (Logcat)         │      │
│  │  (servidor)  │       │  (navegador) │       └──────────────────┘      │
│  └──────────────┘       └──────────────┘                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2. Flujo de Datos: Sensor → Usuario

### PASO 1: Sensor mide CO2 (Arduino)

```
┌─────────────────────────────────────────────────┐
│  Medidor::medirCO2()                            │
│                                                 │
│  ┌─────────────┐     ┌─────────────────────┐    │
│  │ Sensor Fisico│────→│ Valor en ppm (Z)    │    │
│  │ (I2C/SPI)   │     │ Ej: 450 ppm         │    │
│  └─────────────┘     └──────────┬──────────┘    │
│                                 │               │
│  NOTA: En version "NoCambia",  │               │
│  retorna valor fijo = 123       │               │
└─────────────────────────────────┼───────────────┘
                                  │
                                  v
```

### PASO 2: Publicador codifica en iBeacon

```
┌──────────────────────────────────────────────────────────────┐
│  Publicador::publicarCO2(450, 5, 1000)                       │
│                                                              │
│  PASO 2a: Codificar campo MAJOR                              │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  ID_CO2 = 11 (0x0B)                                │     │
│  │  contador = 5                                       │     │
│  │                                                     │     │
│  │  major = (ID_CO2 << 8) + contador                   │     │
│  │        = (0x0B << 8) + 0x05                         │     │
│  │        = 0x0B05                                     │     │
│  │        = [0x0B, 0x05]  (2 bytes)                    │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
│  PASO 2b: El campo MINOR es directamente el valor            │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  minor = 450 = [0x01, 0xC2]  (2 bytes)             │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
│  PASO 2c: Emitir iBeacon                                     │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  EmisoraBLE::emitirAnuncioIBeacon(                  │     │
│  │    uuid   = "EPSG-GTI-PROY-3A"  (16 bytes)         │     │
│  │    major  = 0x0B05              (2 bytes)           │     │
│  │    minor  = 450                 (2 bytes)           │     │
│  │    rssi   = -53                 (1 byte)            │     │
│  │  )                                                   │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
│  PASO 2d: Esperar y detener                                  │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  esperar(1000)  // 1 segundo emitiendo               │     │
│  │  detenerAnuncio()                                   │     │
│  └─────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
                                  │
                                  v
```

### PASO 3: Emisión BLE por radio

```
┌──────────────────────────────────────────────────────────────┐
│  PAQUETE iBeacon transmitido por radio BLE 2.4GHz            │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Byte: 00 01 02 03 04 05 06 07 08 09 10 11 12 13 ... 29│  │
│  │       02 01 06 1a ff 4c 00 02 15 45 50 53 47 2d 47 ...│  │
│  │       ├──advFlags─┤├adv┤├com─┤├t┤├l┤├───UUID──────┤   │  │
│  │                                                    │   │  │
│  │ Byte: 25 26 27 28 29                               │   │  │
│  │       0B 05 01 C2 CB                               │   │  │
│  │       ├──major─┤├minor─┤├txPwr┤                     │   │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Distancia estimada:                                         │
│  d = 10 ^ ((txPower - RSSI) / (10 * n))                     │
│  donde n = 2 (exponente de propagación)                      │
└──────────────────────────────────────────────────────────────┘
                                  │
                           ~10-30 metros
                                  │
                                  v
```

### PASO 4: Smartphone recibe y parsea

```
┌──────────────────────────────────────────────────────────────┐
│  MainActivity::buscarTodosLosDispositivosBTLE()              │
│                                                              │
│  1. BluetoothLeScanner.startScan(callback)                   │
│     └→ Escanea todos los dispositivos BLE cercanos           │
│                                                              │
│  2. onScanResult(callbackType, ScanResult)                   │
│     │                                                        │
│     ├── bluetoothDevice = resultado.getDevice()              │
│     ├── bytes = resultado.getScanRecord().getBytes()         │
│     ├── rssi = resultado.getRssi()                           │
│     │                                                        │
│     └── mostrarInformacionDispositivoBTLE(resultado)         │
│              │                                               │
│              v                                               │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  TramaIBeacon tib = new TramaIBeacon(bytes)         │     │
│  │                                                     │     │
│  │  Parseo:                                            │     │
│  │  prefijo  = bytes[0..8]   = 9 bytes                │     │
│  │  uuid     = bytes[9..24]  = 16 bytes               │     │
│  │  major    = bytes[25..26] = 2 bytes                │     │
│  │  minor    = bytes[27..28] = 2 bytes                │     │
│  │  txPower  = bytes[29]     = 1 byte                 │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
│  3. Mostrar en Logcat:                                       │
│     Log.d(">>>>", " uuid = EPSG-GTI-PROY-3A")              │
│     Log.d(">>>>", " major = 0x0b05 (2821)")                │
│     Log.d(">>>>", " minor = 0x01c2 (450)")                 │
│     Log.d(">>>>", " txPower = 0xcb (-53)")                 │
│                                                              │
│  4. Interpretar:                                             │
│     tipo = major >> 8 = 0x0B = 11 = CO2                     │
│     contador = major & 0xFF = 0x05 = 5                      │
│     valorCO2 = minor = 450 ppm                              │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  ╔═══════════════════════════════════════════╗      │     │
│  │  ║  RESULTADO: CO2 = 450 ppm (iteración 5)  ║      │     │
│  │  ╚═══════════════════════════════════════════╝      │     │
│  │                                                     │     │
│  │  (Mostrado en Logcat, no en UI visible)             │     │
│  └─────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

## 3. Diagrama de Clases Completo

```
┌─────────────────────────────────────────────────────────────────────┐
│                        HARDWARE ARDUINO                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────┐                                                     │
│  │   Medidor    │───┐                                                │
│  │             │   │                                                 │
│  │ -sensores   │   │  composición                                   │
│  └─────────────┘   │                                                │
│                    ▼                                                 │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                   Publicador                         │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ - beaconUUID[16] : Byte = "EPSG-GTI-PROY-3A"       │            │
│  │ - laEmisora      : EmisoraBLE (composición)         │            │
│  │ - RSSI           : Entero = -53                     │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ <<enum>> MedicionesID                               │            │
│  │   CO2 = 11, TEMPERATURA = 12, RUIDO = 13           │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ + encenderEmisora() : Void                          │            │
│  │ + publicarCO2(v:Z, c:Z, t:Z) : Void                │            │
│  │ + publicarTemperatura(v:Z, c:Z, t:Z) : Void        │            │
│  └────────────────────────┬────────────────────────────┘            │
│                           │                                         │
│                           │ composición                             │
│                           ▼                                         │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                   EmisoraBLE                         │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ - nombreEmisora : Texto = "GTI3A-2025"             │            │
│  │ - fabricanteID  : Entero = 0x004C                   │            │
│  │ - txPower       : Entero = 4                        │            │
│  │ - emisor        : BLEDevice*                        │            │
│  │ - servidor      : BLEServer*                        │            │
│  │ - servicio      : BLEService*                       │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ + EmisoraBLE(n, f, t) : EmisoraBLE                 │            │
│  │ + encenderEmisora() : Void                          │            │
│  │ + detenerAnuncio() : Void                           │            │
│  │ + emitirAnuncioIBeacon(u, maj, min, rssi) : Void   │            │
│  │ + emitirAnuncioIBeaconLibre(dat, len) : Void        │            │
│  │ + anyadirServicio(s) : Void                         │            │
│  │ + getConexion() : BLEDevice*                        │            │
│  └─────────────────────────────────────────────────────┘            │
│                                                                     │
│  ┌─────────────────────────┐  ┌─────────────────────────┐          │
│  │         LED             │  │      PuertoSerie         │          │
│  ├─────────────────────────┤  ├─────────────────────────┤          │
│  │ - pin : Entero          │  │ - velocidad : Entero    │          │
│  ├─────────────────────────┤  ├─────────────────────────┤          │
│  │ + LED(p) : LED          │  │ + PuertoSerie(v)        │          │
│  │ + brillar(t) : Void     │  │ + esperarDisponible()   │          │
│  └─────────────────────────┘  │ + escribir(dato) : Void │          │
│                               └─────────────────────────┘          │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        ANDROID (Receptor)                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                 MainActivity                         │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ - ETIQUETA_LOG : Texto = ">>>>"                     │            │
│  │ - CODIGO_PETICION_PERMISOS : Entero                 │            │
│  │ - elEscanner : BluetoothLeScanner                   │            │
│  │ - callbackDelEscaneo : ScanCallback                 │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ + buscarTodosLosDispositivosBTLE() : Void           │            │
│  │ + mostrarInformacionDispositivoBTLE(r) : Void       │            │
│  │ + buscarEsteDispositivoBTLE(nombre) : Void          │            │
│  │ + detenerBusquedaDispositivosBTLE() : Void          │            │
│  │ + inicializarBlueTooth() : Void                     │            │
│  │ + onCreate(savedInstanceState) : Void               │            │
│  └────────────────────────┬────────────────────────────┘            │
│                           │ usa                                     │
│                           ▼                                         │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                 TramaIBeacon                         │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ - prefijo    : Byte[]   (9B)                        │            │
│  │ - uuid       : Byte[]   (16B)                       │            │
│  │ - major      : Byte[]   (2B)                        │            │
│  │ - minor      : Byte[]   (2B)                        │            │
│  │ - txPower    : Byte     (1B)                        │            │
│  │ - advFlags   : Byte[]   (3B)                        │            │
│  │ - advHeader  : Byte[]   (2B)                        │            │
│  │ - companyID  : Byte[]   (2B)                        │            │
│  │ - iBeaconType: Byte     (1B)                        │            │
│  │ - iBeaconLen : Byte     (1B)                        │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ + TramaIBeacon(bytes: Byte[]) : TramaIBeacon        │            │
│  │ + getPrefijo() : Byte[]                             │            │
│  │ + getUUID() : Byte[]                                │            │
│  │ + getMajor() : Byte[]                               │            │
│  │ + getMinor() : Byte[]                               │            │
│  │ + getTxPower() : Byte                               │            │
│  └─────────────────────────────────────────────────────┘            │
│                                                                     │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                 Utilidades                           │            │
│  ├─────────────────────────────────────────────────────┤            │
│  │ + stringToUUID(uuid) : UUID                         │            │
│  │ + bytesToHexString(bytes) : Texto                   │            │
│  │ + bytesToString(bytes) : Texto                      │            │
│  │ + bytesToInt(bytes) : Entero                        │            │
│  │ + bytesToLong(bytes) : Largo                        │            │
│  └─────────────────────────────────────────────────────┘            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 4. Diagrama de Secuencia - Detección de CO2

```
  ARDUINO                    BLE RADIO                 ANDROID
     │                           │                        │
     │  1. medirCO2()            │                        │
     │  ──→ return 450           │                        │
     │                           │                        │
     │  2. publicarCO2(450,5,1000)                       │
     │  ──→ major = 0x0B05       │                        │
     │  ──→ minor = 450          │                        │
     │                           │                        │
     │  3. emitirAnuncioIBeacon  │                        │
     │  ════════════════════════════════════════════════→  │
     │     [UUID: EPSG-GTI-PROY-3A]                      │
     │     [Major: 0x0B05]      │     ScanResult          │
     │     [Minor: 450]         │     ──→ getBytes()      │
     │     [TX: -53]            │     ──→ new TramaIBeacon│
     │                           │          │              │
     │  4. esperar(1000)         │          └──→ getUUID() │
     │     (1 segundo)           │              getMajor() │
     │                           │              getMinor() │
     │  5. detenerAnuncio()      │              getTxPower()│
     │  ════════════════════════════════════════════════→  │
     │                           │          │              │
     │  6. loop siguiente        │          ▼              │
     │  ──→ medirCO2()           │     Logcat:             │
     │  ──→ return 467           │     "uuid=EPSG-GTI-..." │
     │  ──→ publicarCO2(467,6,..)│     "major=0x0B06(2822)"│
     │  ════════════════════════════════════════════════→  │
     │                           │     "minor=0x01D3(467)" │
     │                           │     "txPower=0xcb(-53)" │
```

## 5. Tabla de Mapeo de Valores

| Campo iBeacon | Bits | Contenido | Ejemplo (CO2=450, iter=5) |
|---------------|------|-----------|---------------------------|
| UUID | 128 | Identificador del beacon | `EPSG-GTI-PROY-3A` |
| Major [15..8] | 8 | ID tipo medición | `0x0B` (=11=CO2) |
| Major [7..0] | 8 | Contador iteración | `0x05` (=5) |
| Minor | 16 | Valor de la medición | `0x01C2` (=450) |
| TX Power | 8 | Potencia ref. 1m | `0xCB` (=−53) |

## 6. Interpretación en el Receptor

```java
// PASO 1: Parsear la trama
TramaIBeacon tib = new TramaIBeacon(bytes);

// PASO 2: Extraer campos
byte[] majorBytes = tib.getMajor();  // [0x0B, 0x05]
byte[] minorBytes = tib.getMinor();  // [0x01, 0xC2]

// PASO 3: Convertir a enteros
int majorInt = Utilidades.bytesToInt(majorBytes);  // 0x0B05 = 2821
int minorInt = Utilidades.bytesToInt(minorBytes);  // 0x01C2 = 450

// PASO 4: Decodificar tipo de medición
int tipoMedicion = majorInt >> 8;     // 0x0B = 11 = CO2
int contador = majorInt & 0xFF;       // 0x05 = 5
int valorCO2 = minorInt;              // 450 ppm

// PASO 5: Mostrar al usuario
// Log.d(">>>>", "CO2 = " + valorCO2 + " ppm (iteración " + contador + ")");
```

## 7. Diagrama de Estados del Arduino

```
                 ┌──────────────┐
                 │   APAGADO    │
                 └──────┬───────┘
                        │ setup()
                        ▼
                 ┌──────────────┐
                 │ INICIALIZANDO│
                 │ - PuertoSerie│
                 │ - EmisoraBLE │
                 │ - Medidor    │
                 └──────┬───────┘
                        │
                        ▼
            ┌───────────────────────┐
            │      EMITIENDO        │◄─────────────┐
            │  (publicarCO2/Temp)   │              │
            │                       │              │
            │ 1. emitirAnuncio      │              │
            │ 2. esperar(tiempo)    │              │
            │ 3. detenerAnuncio     │              │
            └───────────┬───────────┘              │
                        │                          │
                        │ loop() siguiente          │
                        └──────────────────────────┘
```

## 8. Diagrama de Estados del Android

```
           ┌─────────────────┐
           │    INICIO       │
           └────────┬────────┘
                    │ onCreate()
                    ▼
           ┌─────────────────┐
           │ INICIALIZANDO   │
           │ Bluetooth       │
           └────────┬────────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
  ┌───────────┐ ┌───────────┐ ┌───────────┐
  │ ESCANEANDO│ │ BUSCANDO  │ │ DETENIDO  │
  │ (todos)   │ │ (nombre)  │ │           │
  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘
        │             │             │
        │ onScanResult│             │
        ▼             ▼             │
  ┌───────────────────────────┐     │
  │ MOSTRANDO INFORMACIÓN     │     │
  │ (mostrarInformacion...)   │     │
  │                           │     │
  │ Parsear TramaIBeacon      │     │
  │ Mostrar UUID, Major, Minor│     │
  └───────────┬───────────────┘     │
              │                     │
              └─────────────────────┘
                    │ stopScan()
                    ▼
              ┌───────────┐
              │ DETENIDO  │
              └───────────┘
```

## 9. Resumen de Tipos de Datos

| Tipo | Dominio | Ejemplo | Uso |
|------|---------|---------|-----|
| Z | Entero | 450, -53, 11 | CO2, RSSI, ID tipo, contador |
| B | Byte | 0x0B, 0x05 | Campos de la trama iBeacon |
| Texto | String | "EPSG-GTI-PROY-3A" | UUID, nombre emisora |
| N | Natural | 1000, 5 | Tiempo de espera, iteraciones |
| UUID | UUID Java | EPSG-GTI-PROY-3A | Identificador del beacon |
