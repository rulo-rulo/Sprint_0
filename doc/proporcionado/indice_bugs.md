# Índice de Bugs y Vulnerabilidades - Sprint_0

## Resumen por Severidad

| Severidad | Cantidad | Descripción                          |
|-----------|----------|--------------------------------------|
| ALTA      | 8        | Bugs críticos de seguridad/funcionalidad |
| MEDIA     | 7        | Bugs de deprecación y diseño         |
| BAJA      | 6        | Bugs menores y código muerto         |

---

## Bugs por Proyecto

### 1. HolaMundoIBeacon (Arduino)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 1 | Valores hardcodeados | BAJA | Medidor.h | 30-37 | `medirCO2()` retorna 123 siempre, `medirTemperatura()` retorna -12. En producción, conectar sensor real. |

### 2. BTLEAlumnos2021 (Android)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 2 | `BluetoothAdapter.enable()` deprecado | MEDIA | MainActivity.java | 218 | Método deprecado desde API 33. Usar `BluetoothAdapter.ACTION_REQUEST_ENABLE`. |
| 3 | ScanFilter no usado | BAJA | MainActivity.java | 162 | Se crea `ScanFilter` con `setDeviceName()` pero no se pasa a `startScan()`. El filtro no tiene efecto. |
| 4 | Falta null check en escáner | BAJA | MainActivity.java | 76 | Si `elEscanner` es null (línea 228), `startScan()` lanzará NullPointerException. |
| 5 | Permisos antiguos | MEDIA | MainActivity.java | 236-244 | Usa `BLUETOOTH` y `BLUETOOTH_ADMIN` que son deprecados. Faltan `BLUETOOTH_SCAN` y `BLUETOOTH_CONNECT` (Android 12+). |

### 3. HolaMundoServicio2021 (Android)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 6 | `IntentService` deprecado | MEDIA | ServicioEscuharBeacons.java | 12 | `IntentService` deprecado desde API 30. Alternativa: `WorkManager` o `JobIntentService`. |
| 7 | Bucle while(true) sinBLE scan real | BAJA | ServicioEscuharBeacons.java | 97 | El servicio ejecuta un bucle con `Thread.sleep()` pero no realiza escaneo BLE real. |
| 8 | `stopSelf()` redundante | BAJA | ServicioEscuharBeacons.java | 60 | `onHandleIntent()` ya detiene el servicio al terminar. `parar()` llama `stopSelf()` innecesariamente. |

### 4. PeticionarioREST (Android)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 9 | `AsyncTask` deprecado | ALTA | PeticionarioREST.java | 16 | `AsyncTask` deprecado desde API 30. Causa memory leaks si la Activity se destruye. Alternativas: Coroutines, RxJava, OkHttp. |
| 10 | `connection.disconnect()` en try | MEDIA | PeticionarioREST.java | 107 | `disconnect()` se llama solo en el caso exitoso. Si hay error, la conexión queda abierta (memory leak). |
| 11 | Sin timeout de conexión | MEDIA | PeticionarioREST.java | 61 | No se configura `connectTimeout` ni `readTimeout`. La petición puede colgar indefinidamente. |
| 12 | `charset-utf-8` malformado | BAJA | PeticionarioREST.java | 62 | Debería ser `charset=UTF-8` (con guion mayúsculas). |

### 5. PruebaRest1-2022 (Android)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 13 | Misma deprecación AsyncTask | ALTA | PeticionarioREST.java | 16 | Mismo bug #9 (copia del PeticionarioREST original). |
| 14 | Misma conexión sin timeout | MEDIA | PeticionarioREST.java | 61 | Mismo bug #11. |

### 6. PBIO-2025-Sprint0 (Android)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 15 | `android.support.v7` en vez de AndroidX | ALTA | MainActivity.java | 4 | Imports de `android.support.v7` son incompatibles con proyectos AndroidX. No compilará con Gradle moderno. |
| 16 | `BluetoothAdapter.enable()` deprecado | MEDIA | MainActivity.java | 301 | Mismo bug #2. |
| 17 | ScanFilter creado pero no usado | BAJA | MainActivity.java | 192 | Crea `ScanFilter` pero pasa lista vacía de filtros. |
| 18 | Doble `startScan()` | MEDIA | MainActivity.java | 210+221 | Llama `startScan()` dos veces: una sin filtros (línea 210) y otra con filtros (línea 221). El segundo sobreescribe el primero. |

### 7. EsqueletoWebAppEnPHPConSesion (Web)

| # | Bug | Severidad | Archivo | Línea | Descripción |
|---|-----|-----------|---------|-------|-------------|
| 19 | Password hardcodeado "1234" | **ALTA** | logica/hacerLogin.php | 12 | Cualquiera puede hacer login con password "1234". Debería usar base de datos con passwords hasheados. |
| 20 | Credenciales por GET | **ALTA** | rest/hacerLogin.php | 25-26 | Nombre y password se envían por GET (visibles en URL, historial del navegador, logs del servidor). Debería usar POST. |
| 21 | Doble `session_start()` | MEDIA | rest/hacerLogin.php | 22,37 | Se llama `session_start()` dos veces. La segunda puede causar warnings. |
| 22 | Sin validación de inputs | MEDIA | rest/hacerLogin.php | 25-26 | No se validan/sanitizan los parámetros `nombre` y `password` de `$_GET`. Riesgo de XSS/SQL injection. |
| 23 | `session_destroy()` en login fallido | BAJA | rest/hacerLogin.php | 41 | Destruye toda la sesión si falla el login, incluso si había otros datos válidos. |

---

## Bugs Críticos Requieren Corrección Inmediata

1. **#19**: Password hardcodeado → Usar `password_hash()` / `password_verify()` con base de datos
2. **#20**: Credenciales por GET → Cambiar a POST con `$_POST[]`
3. **#9/#13**: AsyncTask deprecado → Migrar a Coroutines o WorkManager
4. **#15**: android.support.v7 → Migrar a AndroidX
5. **#2**: BluetoothAdapter.enable() → Usar Intent de configuración
