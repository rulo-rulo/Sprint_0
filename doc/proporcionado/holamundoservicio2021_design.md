# Diseño Lógico - HolaMundoServicio2021 (Android)

## Diagrama de clases (ASCII)

```
+-----------------------------------+
|          MainActivity             |
+-----------------------------------+
| - ETIQUETA_LOG: Texto = ">>>>"    |
| - elIntentDelServicio: Intent     |
+-----------------------------------+
| + botonArrancarServicioPulsado(v: View): void |
| + botonDetenerServicioPulsado(v: View): void  |
| + onCreate(savedInstanceState: Bundle): void   |
+-----------------------------------+
          |
          | crea e inicia
          v
+-----------------------------------+
|       ServicioEscuharBeacons      |
|       (extends IntentService)     |
+-----------------------------------+
| - ETIQUETA_LOG: Texto = ">>>>"    |
| - tiempoDeEspera: Z = 10000       |
| - seguir: B = true                |
+-----------------------------------+
| + ServicioEscuharBeacons(): void  |
| + parar(): void                   |
| + onDestroy(): void               |
| + onHandleIntent(intent: Intent): void |
+-----------------------------------+
```

## Herencia

```
android.app.IntentService
         △
         |
ServicioEscuharBeacons
```

## Flujo de ejecución

```
MainActivity
  │
  ├── botonArrancarServicioPulsado()
  │     ├── elIntentDelServicio = new Intent(this, ServicioEscuharBeacons.class)
  │     ├── elIntentDelServicio.putExtra("tiempoDeEspera", 5000)
  │     └── startService(elIntentDelServicio)
  │
  └── botonDetenerServicioPulsado()
        ├── stopService(elIntentDelServicio)
        └── elIntentDelServicio = null

ServicioEscuharBeacons (WORKER THREAD)
  │
  ├── onHandleIntent(intent)
  │     ├── tiempoDeEspera = intent.getLongExtra("tiempoDeEspera", 50000)
  │     ├── seguir = true
  │     └── while(seguir)
  │           ├── Thread.sleep(tiempoDeEspera)
  │           └── Log("espera: " + contador)
  │
  └── parar()
        ├── seguir = false
        └── stopSelf()
```

## Ciclo de vida del servicio

```
startService() → onCreate() → onStartCommand() → onHandleIntent()
                                                        │
stopService() → onDestroy() → parar() ←─────────────────┘
```

## Constantes

| Constante                    | Tipo  | Valor    | Descripción                     |
|------------------------------|-------|----------|---------------------------------|
| ETIQUETA_LOG                 | Texto | ">>>>"   | Etiqueta para Logcat            |
| tiempoDeEspera (defecto)     | Z     | 10000    | Espera entre ciclos (ms)        |

## Tipos de datos

| Tipo   | Dominio | Descripción                           |
|--------|---------|---------------------------------------|
| Z      | Entero  | Tiempo de espera, contadores          |
| B      | Boolean | Flag de control del bucle             |
| Texto  | String  | Etiquetas de log                      |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
