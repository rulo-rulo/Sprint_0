# Diseño Lógico - PBIO-2025-Sprint0 (Arduino + Android)

## Arduino - Diagrama de clases (ASCII)

```
+-------------------+
|     Publicador    |
+-------------------+
| - beaconUUID: B[16]      |
| - laEmisora: EmisoraBLE  |
| - RSSI: R = -53          |
+-------------------+
| + encenderEmisora(): void                   |
| + publicarCO2(valorCO2:Z, contador:Z, tiempoEspera:Z): void |
| + publicarTemperatura(valorTemperatura:Z, contador:Z, tiempoEspera:Z): void |
+-------------------+

+-------------------+
|     Medidor       |
+-------------------+
| (sin atributos)   |
+-------------------+
| + iniciarMedidor(): void   |
| + medirCO2(): Z = 123      |
| + medirTemperatura(): Z = -12 |
+-------------------+

+-------------------+
| EmisoraBLE        |
+-------------------+
| - nombreEmisora: Texto = "GTI3A-2025" |
| - fabricanteID: Z = 0x004C |
| - txPower: Z = 4          |
+-------------------+
| + emitirAnuncioIBeacon(uuid, major, minor, rssi): void |
| + detenerAnuncio(): void  |
| + encenderEmisora(): void |
+-------------------+
```

## Android - Diagrama de clases (ASCII)

```
+-----------------------------+
|        MainActivity         |
|     (android.support.v7)    |
+-----------------------------+
| - ETIQUETA_LOG: Texto = ">>>>" |
| - CODIGO_PETICION_PERMISOS: Z |
| - elEscanner: BluetoothLeScanner |
| - callbackDelEscaneo: ScanCallback |
+-----------------------------+
| + buscarTodosLosDispositivosBTLE(): void |
| + mostrarInformacionDispositivoBTLE(r: ScanResult): void |
| + buscarEsteDispositivoBTLE(nombre: Texto): void |
| + detenerBusquedaDispositivosBTLE(): void |
| + inicializarBlueTooth(): void |
| + onCreate(savedInstanceState: Bundle): void |
| + onRequestPermissionsResult(...): void |
+-----------------------------+
          |
          | usa
          v
+-----------------------------+
|        TramaIBeacon         |
+-----------------------------+
| - prefijo, uuid, major, minor, txPower: B[] |
| - advFlags, advHeader, companyID: B[] |
| - iBeaconType, iBeaconLength: B |
+-----------------------------+

+-----------------------------+
|        Utilidades           |
+-----------------------------+
| + stringToUUID(uuid: Texto): UUID |
| + bytesToHexString(bytes: B[]): Texto |
| + bytesToInt(bytes: B[]): Z |
+-----------------------------+
```

## Diferencias vs版本 2021

| Característica       | BTLEAlumnos2021          | PBIO-2025-Sprint0         |
|---------------------|--------------------------|----------------------------|
| Framework           | AndroidX                 | android.support.v7         |
| Permisos            | BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION | + BLUETOOTH_SCAN, BLUETOOTH_CONNECT |
| ScanFilter          | No usado como filtro real | Creado pero no aplicado    |
| ScanSettings        | No configurado           | SCAN_MODE_LOW_LATENCY      |
| Nombre beacon       | "fistro"                 | "GTI3A-2025"              |
| UUID beacon         | "EPSG-GTI-PROY-3A"      | "EPSG-GTI-PROY-3A"        |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
