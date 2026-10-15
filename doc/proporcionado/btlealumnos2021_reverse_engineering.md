# Ingeniería Inversa - BTLEAlumnos2021 (Detección de Beacons)

## 1. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SISTEMA DE DETECCIÓN BLE                              │
│                                                                         │
│  CAPA HARDWARE                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                                                                  │   │
│  │  ┌─────────────┐  Radio   ┌─────────────┐                       │   │
│  │  │  EMISOR     │─────────→│  RECEPTOR   │                       │   │
│  │  │  (Arduino   │  BLE     │  (Android   │                       │   │
│  │  │   Nano 33)  │  2.4GHz  │   Phone)    │                       │   │
│  │  └─────────────┘          └─────────────┘                       │   │
│  │       │                        │                                 │   │
│  │       ▼                        ▼                                 │   │
│  │  EmisoraBLE              BluetoothAdapter                        │   │
│  │  .emitirAnuncio          .getBluetoothLeScanner()                │   │
│  │   iBeacon()                     │                                │   │
│  │                                 ▼                                │   │
│  │                          BluetoothLeScanner                      │   │
│  │                          .startScan(callback)                    │   │
│  │                                 │                                │   │
│  │                                 ▼                                │   │
│  │                          ScanCallback                            │   │
│  │                          .onScanResult()                         │   │
│  │                                                                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  CAPA ANDROID                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                                                                  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  MainActivity                                              │  │   │
│  │  │  - inicializarBlueTooth()                                   │  │   │
│  │  │  - buscarTodosLosDispositivosBTLE()                        │  │   │
│  │  │  - buscarEsteDispositivoBTLE(nombre)                       │  │   │
│  │  │  - detenerBusquedaDispositivosBTLE()                       │  │   │
│  │  │  - mostrarInformacionDispositivoBTLE(resultado)            │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │                          │                                       │   │
│  │                          ▼                                       │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  TramaIBeacon                                              │  │   │
│  │  │  - Constructor: parsea bytes del ScanResult                │  │   │
│  │  │  - Getters: prefijo, uuid, major, minor, txPower           │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │                          │                                       │   │
│  │                          ▼                                       │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  Utilidades                                                │  │   │
│  │  │  - bytesToHexString() → mostrar trama en hexadecimal       │  │   │
│  │  │  - bytesToString()    → mostrar UUID como texto            │  │   │
│  │  │  - bytesToInt()       → convertir major/minor a entero     │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │                                                                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2. Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MainActivity                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Atributos:                                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ - ETIQUETA_LOG         : Texto = ">>>>"                      │   │
│  │ - CODIGO_PETICION_PERMISOS : Entero = 11223344               │   │
│  │ - elEscanner           : BluetoothLeScanner                  │   │
│  │ - callbackDelEscaneo   : ScanCallback                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Métodos públicos (botones):                                        │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + botonBuscarDispositivosBTLEPulsado(v:View) : Void          │   │
│  │   └→ buscarTodosLosDispositivosBTLE()                        │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ + botonBuscarNuestroDispositivoBTLEPulsado(v:View) : Void    │   │
│  │   └→ buscarEsteDispositivoBTLE("fistro")                     │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ + botonDetenerBusquedaDispositivosBTLEPulsado(v:View) : Void │   │
│  │   └→ detenerBusquedaDispositivosBTLE()                       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Métodos privados (lógica):                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ - inicializarBlueTooth() : Void                               │   │
│  │   1. BluetoothAdapter.getDefaultAdapter()                    │   │
│  │   2. bta.enable()                                            │   │
│  │   3. bta.getBluetoothLeScanner()                             │   │
│  │   4. Solicitar permisos (BLUETOOTH, BLUETOOTH_ADMIN,         │   │
│  │      ACCESS_FINE_LOCATION)                                   │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - buscarTodosLosDispositivosBTLE() : Void                    │   │
│  │   1. Crear ScanCallback (onScanResult, onBatch, onFailed)    │   │
│  │   2. elEscanner.startScan(callback)                          │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - buscarEsteDispositivoBTLE(nombre) : Void                   │   │
│  │   1. Crear ScanCallback                                      │   │
│  │   2. Crear ScanFilter con setDeviceName(nombre)              │   │
│  │   3. elEscanner.startScan(callback)                          │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - detenerBusquedaDispositivosBTLE() : Void                   │   │
│  │   1. elEscanner.stopScan(callback)                           │   │
│  │   2. callbackDelEscaneo = null                               │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - mostrarInformacionDispositivoBTLE(r:ScanResult) : Void     │   │
│  │   1. bluetoothDevice = r.getDevice()                         │   │
│  │   2. bytes = r.getScanRecord().getBytes()                    │   │
│  │   3. rssi = r.getRssi()                                      │   │
│  │   4. tib = new TramaIBeacon(bytes)                           │   │
│  │   5. Mostrar en Logcat: nombre, MAC, RSSI, UUID, major, minor│   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ usa
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        TramaIBeacon                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Atributos:                                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ - prefijo     : Byte[]  (9 bytes)   [advFlags|advH|compID]  │   │
│  │ - uuid        : Byte[]  (16 bytes)                           │   │
│  │ - major       : Byte[]  (2 bytes)                            │   │
│  │ - minor       : Byte[]  (2 bytes)                            │   │
│  │ - txPower     : Byte    (1 byte)                             │   │
│  │ - losBytes    : Byte[]  (bytes originales)                   │   │
│  │ - advFlags    : Byte[]  (3 bytes)                            │   │
│  │ - advHeader   : Byte[]  (2 bytes)                            │   │
│  │ - companyID   : Byte[]  (2 bytes)                            │   │
│  │ - iBeaconType : Byte    (1 byte)                             │   │
│  │ - iBeaconLen  : Byte    (1 byte)                             │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Constructor:                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + TramaIBeacon(bytes: Byte[])                                │   │
│  │   prefijo  = bytes[0..8]                                     │   │
│  │   uuid     = bytes[9..24]                                    │   │
│  │   major    = bytes[25..26]                                   │   │
│  │   minor    = bytes[27..28]                                   │   │
│  │   txPower  = bytes[29]                                       │   │
│  │   advFlags = prefijo[0..2]                                   │   │
│  │   advHeader= prefijo[3..4]                                   │   │
│  │   companyID= prefijo[5..6]                                   │   │
│  │   iBeaconType  = prefijo[7]                                  │   │
│  │   iBeaconLength= prefijo[8]                                  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Getters:                                                           │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + getPrefijo() : Byte[]      + getAdvFlags() : Byte[]        │   │
│  │ + getUUID() : Byte[]         + getAdvHeader() : Byte[]       │   │
│  │ + getMajor() : Byte[]        + getCompanyID() : Byte[]       │   │
│  │ + getMinor() : Byte[]        + getiBeaconType() : Byte       │   │
│  │ + getTxPower() : Byte        + getiBeaconLength() : Byte     │   │
│  │ + getLosBytes() : Byte[]                                      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ usa
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Utilidades                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Métodos estáticos:                                                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + stringToBytes(texto:Texto) : Byte[]                        │   │
│  │ + stringToUUID(uuid:Texto) : UUID                            │   │
│  │ + uuidToString(uuid:UUID) : Texto                            │   │
│  │ + uuidToHexString(uuid:UUID) : Texto                         │   │
│  │ + bytesToString(bytes:Byte[]) : Texto                        │   │
│  │ + dosLongToBytes(mas:Z, menos:Z) : Byte[]                   │   │
│  │ + bytesToInt(bytes:Byte[]) : Z                               │   │
│  │ + bytesToLong(bytes:Byte[]) : Z                              │   │
│  │ + bytesToHexString(bytes:Byte[]) : Texto                     │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 3. Diagrama de Secuencia - Detección Completa

```
  EMISOR (Arduino)        RECEPTOR (Android)           USUARIO
       │                        │                        │
       │ 1. emitirAnuncio       │                        │
       │    iBeacon()           │                        │
       │ ═══════════════════════│═══════════════════════→│
       │   [UUID, Major,        │                        │
       │    Minor, TX]          │                        │
       │                        │                        │
       │                   2. ScanCallback                │
       │                      .onScanResult()            │
       │                        │                        │
       │                   3. resultado.getDevice()      │
       │                      .getName() → "fistro"     │
       │                      .getAddress() → "AA:BB:.."│
       │                        │                        │
       │                   4. resultado.getScanRecord()  │
       │                      .getBytes() → bytes[]     │
       │                        │                        │
       │                   5. new TramaIBeacon(bytes)    │
       │                        │                        │
       │                        │  ┌──────────────────┐  │
       │                        │  │ Parseo:          │  │
       │                        │  │ uuid = bytes[9]  │  │
       │                        │  │ major= bytes[25] │  │
       │                        │  │ minor= bytes[27] │  │
       │                        │  │ txPwr= bytes[29] │  │
       │                        │  └──────────────────┘  │
       │                        │                        │
       │                   6. Log.d(">>>>",             │
       │                      "uuid = EPSG-GTI-...")    │
       │                        │                        │
       │                   7. Log.d(">>>>",             │
       │                      "major = 0x0B05 (2821)")  │
       │                        │                        │
       │                   8. Log.d(">>>>",             │
       │                      "minor = 0x01C2 (450)")   │
       │                        │                        │
       │                   9. Log.d(">>>>",             │
       │                      "txPower = 0xcb (-53)")   │
       │                        │                        │
       │                        │  ┌──────────────────┐  │
       │                        │  │ INTERPRETACIÓN:  │  │
       │                        │  │ tipo = maj>>8    │  │
       │                        │  │    = 11 = CO2    │  │
       │                        │  │ cont = maj&0xFF  │  │
       │                        │  │    = 5           │  │
       │                        │  │ val = minor      │  │
       │                        │  │    = 450 ppm     │  │
       │                        │  └──────────────────┘  │
       │                        │                        │
       │                        │  ┌──────────────────┐  │
       │                        │  │ LOGCAT:          │──→ "CO2 = 450"
       │                        │  │ ">>>> CO2=450"   │  │  (visible en
       │                        │  └──────────────────┘  │   Android Studio)
       │                        │                        │
```

## 4. Formato de la Trama iBeacon (30 bytes)

```
┌─────────────────────────────────────────────────────────────────┐
│  TRAMA iBeacon - Estructura de 30 bytes                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Byte   Contenido                 Tamaño    Descripción         │
│  ─────  ─────────────────────    ──────    ─────────────────   │
│  [0..2] advFlags                  3B        Flags BLE (02 01 06)│
│  [3..4] advHeader                 2B        Cabecera anuncio    │
│  [5..6] companyID                 2B        ID fabricante       │
│                                          (004C = Apple)        │
│  [7]    iBeaconType               1B        Tipo (02=proximity) │
│  [8]    iBeaconLength             1B        Longitud (15=21B)   │
│  [9..24] UUID                     16B       Identificador único │
│                                          "EPSG-GTI-PROY-3A"   │
│  [25..26] Major                   2B        Tipo+contenedor    │
│                                          [tipo<<8 | contador]  │
│  [27..28] Minor                   2B        Valor de medición   │
│  [29]    TX Power                 1B        Ref. potencia 1m   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 5. Flujo de Escaneo - Modo Todos

```
┌─────────────────────────────────────────────────────────────────┐
│  MODO: ESCANEAR TODOS LOS DISPOSITIVOS                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Usuario pulsa "Buscar dispositivos BTLE"                    │
│     └→ botonBuscarDispositivosBTLEPulsado()                     │
│        └→ buscarTodosLosDispositivosBTLE()                      │
│                                                                 │
│  2. Crear ScanCallback:                                         │
│     ┌────────────────────────────────────────────────────────┐  │
│     │ onScanResult(callbackType, resultado) {                │  │
│     │   mostrarInformacionDispositivoBTLE(resultado);        │  │
│     │ }                                                      │  │
│     │                                                        │  │
│     │ onBatchScanResults(results) { /* log */ }              │  │
│     │ onScanFailed(errorCode) { /* log */ }                  │  │
│     └────────────────────────────────────────────────────────┘  │
│                                                                 │
│  3. elEscanner.startScan(callback)                              │
│     (sin filtro → todos los dispositivos BTLE)                  │
│                                                                 │
│  4. Por cada dispositivo detectado:                             │
│     └→ mostrarInformacionDispositivoBTLE()                      │
│        ├→ Log: nombre, MAC, RSSI                               │
│        ├→ new TramaIBeacon(bytes)                               │
│        └→ Log: prefijo, UUID, major, minor, txPower            │
│                                                                 │
│  5. Para detener:                                               │
│     └→ botonDetenerBusquedaDispositivosBTLEPulsado()           │
│        └→ detenerBusquedaDispositivosBTLE()                    │
│           ├→ elEscanner.stopScan(callback)                      │
│           └→ callback = null                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Flujo de Escaneo - Modo Filtrado

```
┌─────────────────────────────────────────────────────────────────┐
│  MODO: BUSCAR DISPOSITIVO ESPECÍFICO POR NOMBRE                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Usuario pulsa "Nuestro dispositivo BTLE"                    │
│     └→ botonBuscarNuestroDispositivoBTLEPulsado()              │
│        └→ buscarEsteDispositivoBTLE("fistro")                   │
│                                                                 │
│  2. Crear ScanCallback (igual que modo todos)                   │
│                                                                 │
│  3. Crear ScanFilter:                                           │
│     ┌────────────────────────────────────────────────────────┐  │
│     │ ScanFilter sf = new ScanFilter.Builder()               │  │
│     │   .setDeviceName("fistro")                             │  │
│     │   .build();                                            │  │
│     └────────────────────────────────────────────────────────┘  │
│                                                                 │
│  4. elEscanner.startScan(callback)                              │
│     (solo detecta dispositivos con nombre "fistro")             │
│                                                                 │
│  5. Muestra info SOLO del dispositivo filtrado                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 7. Diagrama de Estados - Escáner BLE

```
                 ┌──────────────────┐
                 │   SIN ESCANEAR   │
                 │  (callback=null) │
                 └────────┬─────────┘
                          │
          startScan()     │     stopScan()
          ────────────────┼───────────────
                          │
                          ▼
                 ┌──────────────────┐
                 │    ESCANEANDO    │
                 │ (callback activo)│
                 └────────┬─────────┘
                          │
                    onScanResult()
                          │
                          ▼
                 ┌──────────────────┐
                 │  MOSTRANDO INFO  │
                 │ (TramaIBeacon)   │
                 └────────┬─────────┘
                          │
                          │ (vuelve a escanear)
                          └──→ ESCANEANDO
```

## 8. Resumen de Tipos de Datos

| Tipo | Dominio | Ejemplo | Uso |
|------|---------|---------|-----|
| Z | Entero | 450, -53, 11 | RSSI, valor CO2, ID tipo |
| B | Byte | 0x0B, 0x05 | Campos de la trama iBeacon |
| Texto | String | "fistro", "EPSG-GTI-PROY-3A" | Nombre beacon, UUID |
| UUID | UUID Java | EPSG-GTI-PROY-3A | Identificador del beacon |
