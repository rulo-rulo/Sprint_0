# Flujo Global: Sensor → BLE → Android → Usuario

## Diagrama Completo del Sistema IoT

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                                                                 │
│  ╔═══════════════════════════════════════════════════════════════════════════╗   │
│  ║                    FASE 1: CAPTURA DEL DATO (Arduino)                    ║   │
│  ╚═══════════════════════════════════════════════════════════════════════════╝   │
│                                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐      │
│  │   SENSOR    │───→│   MEDIDOR   │───→│  PUBLICADOR │───→│  EMISORBLE  │      │
│  │  FÍSICO     │    │  (wrapper)  │    │ (codifica)  │    │  (emite)    │      │
│  │             │    │             │    │             │    │             │      │
│  │ Ej: SCD30   │    │ medirCO2() │    │ publicarCO2 │    │ emitir      │      │
│  │ (I2C/SPI)   │    │ →450 ppm   │    │ (v,c,t)     │    │ iBeacon()   │      │
│  └─────────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘      │
│                                                                  │             │
│  Datos:                                                          │             │
│  ─────                                                           │             │
│  Sensor → 450 ppm CO2                                           │             │
│  Medidor → 450 (entero)                                         │             │
│  Publicador → MAJOR = (11<<8)+5 = 0x0B05                        │             │
│               MINOR = 450 = 0x01C2                              │             │
│               UUID = "EPSG-GTI-PROY-3A"                         │             │
│  Emisor → Paquete iBeacon de 30 bytes por radio BLE             │             │
│                                                                  │             │
│  ══════════════════════════════════════════════════════════════════════════     │
│                           RADIO BLE 2.4GHz                                     │
│  ══════════════════════════════════════════════════════════════════════════     │
│                                              │                                 │
│                                              │  ~10-30 metros                  │
│                                              │                                 │
│  ╔═══════════════════════════════════════════════════════════════════════════╗   │
│  ║                    FASE 2: RECEPCIÓN (Android)                           ║   │
│  ╚═══════════════════════════════════════════════════════════════════════════╝   │
│                                              │                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────┴───────┐                       │
│  │ BLUETOOTHLE │───→│   SCAN      │───→│  ONSCAN     │                       │
│  │ SCANNER     │    │   CALLBACK  │    │  RESULT()   │                       │
│  │             │    │             │    │             │                       │
│  │ startScan() │    │ onScan      │    │ ScanResult  │                       │
│  │             │    │ Result()    │    │ → getDevice │                       │
│  └─────────────┘    └─────────────┘    │ → getBytes  │                       │
│                                        │ → getRssi   │                       │
│                                        └──────┬──────┘                       │
│                                               │                              │
│                                               ▼                              │
│  ╔═══════════════════════════════════════════════════════════════════════════╗   │
│  ║                    FASE 3: PARSEO (Android)                              ║   │
│  ╚═══════════════════════════════════════════════════════════════════════════╝   │
│                                               │                              │
│  ┌─────────────┐    ┌─────────────┐    ┌──────┴───────┐                       │
│  │ TRAMA       │───→│  UTILIDADES │───→│  MAINACTIVITY│                       │
│  │ iBEACON     │    │  (convierte)│    │  (muestra)   │                       │
│  │ (parsea)    │    │             │    │             │                       │
│  │             │    │ bytesToInt()│    │ Log.d()     │                       │
│  │ new(bytes)  │    │ bytesToHex()│    │ "uuid=..."  │                       │
│  └─────────────┘    └─────────────┘    │ "major=..." │                       │
│                                        │ "minor=..." │                       │
│  Datos parseados:                      │ "txPwr=..." │                       │
│  ───────────────                       └──────┬──────┘                       │
│  UUID = EPSG-GTI-PROY-3A                      │                              │
│  Major = 0x0B05 (tipo=CO2, cont=5)            │                              │
│  Minor = 450 (valor CO2 en ppm)               │                              │
│  TxPower = -53 dBm                           │                              │
│                                               │                              │
│  ╔═══════════════════════════════════════════════════════════════════════════╗   │
│  ║                    FASE 4: VISUALIZACIÓN (Usuario)                       ║   │
│  ╚═══════════════════════════════════════════════════════════════════════════╝   │
│                                               │                              │
│                                               ▼                              │
│  ┌─────────────────────────────────────────────────────────────────────┐       │
│  │                                                                     │       │
│  │  ╔═══════════════════════════════════════════════════════════╗      │       │
│  │  ║  ANDROID STUDIO - LOGCAT                                  ║      │       │
│  │  ╠═══════════════════════════════════════════════════════════╣      │       │
│  │  ║  >>>>  ****************************************************║      │       │
│  │  ║  >>>>  ****** DISPOSITIVO DETECTADO BTLE **************** ║      │       │
│  │  ║  >>>>  ****************************************************║      │       │
│  │  ║  >>>>  nombre = fistro                                    ║      │       │
│  │  ║  >>>>  dirección = AA:BB:CC:DD:EE:FF                     ║      │       │
│  │  ║  >>>>  rssi = -45                                        ║      │       │
│  │  ║  >>>>  prefijo  = 02:01:06:1a:ff:4c:00:02:15             ║      │       │
│  │  ║  >>>>  uuid  = 45:50:53:47:2d:47:54:49:2d...            ║      │       │
│  │  ║  >>>>  uuid  = EPSG-GTI-PROY-3A                         ║      │       │
│  │  ║  >>>>  major  = 0b:05 (2821)                             ║      │       │
│  │  ║  >>>>  minor  = 01:c2 (450)                              ║      │       │
│  │  ║  >>>>  txPower  = cb (-53)                                ║      │       │
│  │  ╚═══════════════════════════════════════════════════════════╝      │       │
│  │                                                                     │       │
│  │  INTERPRETACIÓN:                                                    │       │
│  │  ───────────────                                                    │       │
│  │  tipo = major >> 8 = 11 = CO2                                       │       │
│  │  contador = major & 0xFF = 5                                         │       │
│  │  valor = minor = 450 ppm CO2                                        │       │
│  │                                                                     │       │
│  │  ╔═══════════════════════════════════════════════════════════╗      │       │
│  │  ║  RESULTADO FINAL:                                         ║      │       │
│  │  ║  "El sensor detectó 450 ppm de CO2 en la iteración 5"    ║      │       │
│  │  ╚═══════════════════════════════════════════════════════════╝      │       │
│  │                                                                     │       │
│  └─────────────────────────────────────────────────────────────────────┘       │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Secuencia Temporal Completa

```
TIEMPO  EVENTO                                      DATOS
──────  ──────────────────────────────────────────  ─────────────────────────
  0s    setup() completa                            Puerto serie listo
  1s    emisora encendida                           BLE activo
  2s    medirCO2() → 450                            entero 450
  2s    publicarCO2(450, 1, 1000)                   major=0x0B01, minor=450
  2s    emitirAnuncioIBeacon(UUID, maj, min, -53)   paquete 30 bytes
  2s    ══════ EMISIÓN BLE ══════                   radio 2.4GHz
  3s    esperar(1000)                               (1 segundo emitiendo)
  3s    detenerAnuncio()                            fin de emisión
  3s    loop() → medirCO2() → 467                   entero 467
  3s    publicarCO2(467, 2, 1000)                   major=0x0B02, minor=467
  3s    emitirAnuncioIBeacon(...)                   paquete 30 bytes
  3s    ══════ EMISIÓN BLE ══════                   radio 2.4GHz
  3s    Android: onScanResult()                     ScanResult recibido
  3s    new TramaIBeacon(bytes)                     parseo completado
  3s    Logcat: "uuid=EPSG-GTI-..."                visualización
  3s    Logcat: "major=0x0B02 (2822)"              tipo=CO2, cont=2
  3s    Logcat: "minor=0x01D3 (467)"               valor=467 ppm
  4s    esperar(1000)                               (1 segundo emitiendo)
  4s    detenerAnuncio()                            fin de emisión
  ...   (ciclo se repite indefinidamente)
```

## Diagrama de Componentes del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│  HARDWARE                    SOFTWARE                 USUARIO           │
│  ────────                    ────────                 ───────           │
│                                                                         │
│  ┌──────────┐              ┌──────────┐              ┌──────────┐      │
│  │ SENSOR   │              │ ARDUINO  │              │ ANDROID  │      │
│  │ CO2      │──── I2C ───→│ Medidor  │              │          │      │
│  │          │              │          │              │ Logcat   │      │
│  └──────────┘              │ Public.  │──── BLE ───→│          │      │
│                            │          │   Radio     │ Trama    │      │
│  ┌──────────┐              │ Emisora  │              │ IBeacon  │      │
│  │ LED      │←── GPIO ────│ BLE      │              │          │      │
│  │ (pin 7)  │              │          │              │ Utilid.  │      │
│  └──────────┘              └──────────┘              └──────────┘      │
│                                                                         │
│  ┌──────────┐              ┌──────────┐              ┌──────────┐      │
│  │ PUERTO   │←── UART ────│ PuertoSerie│             │ RESULTADO│      │
│  │ SERIE    │              │          │              │ "CO2=450"│      │
│  │ (USB)    │              └──────────┘              └──────────┘      │
│  └──────────┘                                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Tabla Resumen de Proyectos

| Proyecto | Tecnología | Función | Entrada | Salida |
|----------|------------|---------|---------|--------|
| HolaMundoIBeacon | Arduino C++ | Emite iBeacon con CO2 | Sensor CO2 | Radio BLE |
| BTLEAlumnos2021 | Java Android | Escanea y parsea iBeacon | Radio BLE | Logcat |
| PBIO-2025 | Arduino + Android | Sistema completo IoT | Sensor CO2 | Logcat |
| PeticionarioREST | Java Android | Cliente HTTP REST | URL + body | JSON |
| EsqueletoWebApp | PHP + JS + HTML | Webapp con sesiones | Formulario | HTML |
| HolaMundoServicio | Java Android | Servicio en 2do plano | Timer | Logcat |
