# Diseño Lógico - BTLEAlumnos2021 (Android)

## Diagrama de clases (ASCII)

```
+-----------------------------+
|        MainActivity         |
+-----------------------------+
| - ETIQUETA_LOG: Texto = ">>>>" |
| - CODIGO_PETICION_PERMISOS: Z = 11223344 |
| - elEscanner: BluetoothLeScanner |
| - callbackDelEscaneo: ScanCallback |
+-----------------------------+
| + buscarTodosLosDispositivosBTLE(): void |
| + mostrarInformacionDispositivoBTLE(resultado: ScanResult): void |
| + buscarEsteDispositivoBTLE(dispositivoBuscado: Texto): void |
| + detenerBusquedaDispositivosBTLE(): void |
| + botonBuscarDispositivosBTLEPulsado(v: View): void |
| + botonBuscarNuestroDispositivoBTLEPulsado(v: View): void |
| + botonDetenerBusquedaDispositivosBTLEPulsado(v: View): void |
| + inicializarBlueTooth(): void |
| + onCreate(savedInstanceState: Bundle): void |
| + onRequestPermissionsResult(requestCode: Z, permissions: Texto[], grantResults: Z[]): void |
+-----------------------------+
          |
          | usa
          v
+-----------------------------+
|        TramaIBeacon         |
+-----------------------------+
| - prefijo: B[]              |  9 bytes
| - uuid: B[]                 |  16 bytes
| - major: B[]                |  2 bytes
| - minor: B[]                |  2 bytes
| - txPower: B                |  1 byte
| - losBytes: B[]             |  bytes originales
| - advFlags: B[]             |  3 bytes
| - advHeader: B[]            |  2 bytes
| - companyID: B[2]           |  2 bytes
| - iBeaconType: B            |  1 byte
| - iBeaconLength: B          |  1 byte
+-----------------------------+
| + getPrefijo(): B[]         |
| + getUUID(): B[]            |
| + getMajor(): B[]           |
| + getMinor(): B[]           |
| + getTxPower(): B           |
| + getLosBytes(): B[]        |
| + getAdvFlags(): B[]        |
| + getAdvHeader(): B[]       |
| + getCompanyID(): B[]       |
| + getiBeaconType(): B       |
| + getiBeaconLength(): B     |
| + TramaIBeacon(bytes: B[])  |  ← constructor
+-----------------------------+

+-----------------------------+
|        Utilidades           |
+-----------------------------+
| (sin atributos - todos los métodos son estáticos) |
+-----------------------------+
| + stringToBytes(texto: Texto): B[] |
| + stringToUUID(uuid: Texto): UUID |
| + uuidToString(uuid: UUID): Texto |
| + uuidToHexString(uuid: UUID): Texto |
| + bytesToString(bytes: B[]): Texto |
| + dosLongToBytes(mas: Z, menos: Z): B[] |
| + bytesToInt(bytes: B[]): Z |
| + bytesToLong(bytes: B[]): Z |
| + bytesToIntOK(bytes: B[]): Z |
| + bytesToHexString(bytes: B[]): Texto |
+-----------------------------+
```

## Constantes

| Constante                    | Tipo  | Valor      | Descripción                   |
|------------------------------|-------|------------|-------------------------------|
| ETIQUETA_LOG                 | Texto | ">>>>"     | Etiqueta para Logcat          |
| CODIGO_PETICION_PERMISOS     | Z     | 11223344   | Código de petición de permisos |

## Flujo de escaneo BTLE

```
buscarTodosLosDispositivosBTLE()
  ├── Crear ScanCallback (onScanResult, onBatchScanResults, onScanFailed)
  └── elEscanner.startScan(callback)

buscarEsteDispositivoBTLE(nombre)
  ├── Crear ScanCallback
  ├── Crear ScanFilter con setDeviceName(nombre)
  └── elEscanner.startScan(callback)

mostrarInformacionDispositivoBTLE(resultado)
  ├── bluetoothDevice = resultado.getDevice()
  ├── bytes = resultado.getScanRecord().getBytes()
  ├── rssi = resultado.getRssi()
  ├── Mostrar nombre, MAC, RSSI
  ├── tib = new TramaIBeacon(bytes)
  └── Mostrar prefijo, UUID, major, minor, txPower
```

## Formato iBeacon (30 bytes)

```
[0..8]   prefijo (9B)    = advFlags(3) + advHeader(2) + companyID(2) + type(1) + length(1)
[9..24]  uuid (16B)      = Identificador único del beacon
[25..26] major (2B)      = Campo mayor (tipo medida + contador)
[27..28] minor (2B)      = Campo menor (valor de la medida)
[29]     txPower (1B)    = Potencia de referencia a 1 metro
```

## Tipos de datos

| Tipo   | Dominio    | Descripción                          |
|--------|------------|--------------------------------------|
| Z      | Entero     | RSSI, códigos, índices               |
| B      | Byte       | Bytes de la trama iBeacon            |
| Texto  | String     | Nombres, direcciones MAC             |
| UUID   | java.util.UUID | Identificador único del beacon   |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
