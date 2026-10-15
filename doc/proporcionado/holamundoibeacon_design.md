# Diseño Lógico - HolaMundoIBeacon (Arduino)

## Diagrama de clases (ASCII)

```
+-------------------+
|     Publicador    |
+-------------------+
| - beaconUUID: B[16]      |
| - laEmisora: EmisoraBLE  |
| - RSSI: R                |
+-------------------+
| + encenderEmisora(): void                   |
| + publicarCO2(valorCO2:Z, contador:Z, tiempoEspera:Z): void |
| + publicarTemperatura(valorTemperatura:Z, contador:Z, tiempoEspera:Z): void |
+-------------------+
          |
          | usa
          v
+-------------------+
|    EmisoraBLE     |
+-------------------+
| - nombreEmisora: Texto  |
| - fabricanteID: Z       |
| - txPower: Z            |
| - emisor: BLEDevice*    |
| - servidor: BLEServer*  |
| - servicio: BLEService* |
+-------------------+
| + EmisoraBLE(nombre, fabricanteID, txPower) |
| + encenderEmisora(): void                   |
| + detenerAnuncio(): void                    |
| + emitirAnuncioIBeacon(uuid, major, minor, rssi): void |
| + emitirAnuncioIBeaconLibre(datos, longitud): void |
| + anyadirServicio(servicio): void           |
| + getConexion(): BLEDevice*                 |
+-------------------+
          |
          | compone
          v
+-------------------+
| ServicioEnEmisora |
+-------------------+
| - servicio: BLEService*           |
| - emisora: EmisoraBLE*            |
| - caracteristica: BLECharacteristic* |
+-------------------+
| + ServicioEnEmisora(uuid, emisora) |
| + anyadirCaracteristica(c): void   |
| + getServicio(): BLEService*       |
+-------------------+

+-------------------+
|     Medidor       |
+-------------------+
| (sin atributos)   |
+-------------------+
| + iniciarMedidor(): void   |
| + medirCO2(): Z            |
| + medirTemperatura(): Z    |
+-------------------+

+-------------------+
|        LED        |
+-------------------+
| - pin: Z          |
+-------------------+
| + LED(pin): void        |
| + brillar(tiempo: Z): void |
+-------------------+

+-------------------+
|   PuertoSerie     |
+-------------------+
| - velocidad: Z    |
+-------------------+
| + PuertoSerie(velocidad): void |
| + esperarDisponible(): void    |
| + escribir(dato): void         |
+-------------------+
```

## Namespace Globales

| Variable      | Tipo           | Descripción                      |
|---------------|----------------|----------------------------------|
| elLED         | LED            | LED indicador en pin 7           |
| elPuerto      | PuertoSerie    | Puerto serie a 115200 baudios    |
| elPublicador  | Publicador     | Emisor de anuncios iBeacon       |
| elMedidor     | Medidor        | Sensor CO2/temperatura (valores fijos) |

## Namespace Loop

| Variable | Tipo   | Descripción                  |
|----------|--------|------------------------------|
| cont     | uint8  | Contador de iteraciones      |

## Funciones globales

| Función              | Descripción                                    |
|----------------------|------------------------------------------------|
| `setup()`            | Inicialización: puerto serie, emisora, sensor   |
| `loop()`             | Bucle principal: mide CO2, publica iBeacon      |
| `lucecitas()`        | Secuencia de parpadeo del LED                   |
| `inicializarPlaquita()` | Inicialización adicional de la placa          |

## Flujo de ejecución

```
setup()
  ├── elPuerto.esperarDisponible()
  ├── inicializarPlaquita()
  ├── elPublicador.encenderEmisora()
  ├── elMedidor.iniciarMedidor()
  └── esperar(1000)

loop()
  ├── cont++
  ├── lucecitas()
  ├── valorCO2 = elMedidor.medirCO2()
  ├── elPublicador.publicarCO2(valorCO2, cont, 1000)
  └── esperar(tiempoEspera)
```

## Tipos de datos

| Tipo     | Dominio         | Descripción                     |
|----------|-----------------|---------------------------------|
| Z        | Entero          | Valores de CO2, temperatura, RSSI |
| B        | Byte            | UUID, campos BLE                |
| Texto    | String          | Nombre de emisora               |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
