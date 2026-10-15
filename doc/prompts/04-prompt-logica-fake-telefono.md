# Prompt 04 — Lógica fake en el teléfono

> Prompt para la IA. Objeto: generar la **lógica fake** que corre en el teléfono
> (Java puro), sistema descrito en diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente
> estas decisiones; no introduzcas funcionalidades no pedidas. Esta pieza simula la
> detección del beacon y el envío al servidor para poder testear el sistema sin
> dispositivo físico.
>
> Dependencia funcional (no de generación): el `TransporteREST` por defecto apunta
> a la ruta del prompt 03; los tests usan siempre un transporte falso inyectado.

---

## 1. Diseño gráfico de la pieza

```
 UI del teléfono (solo diseño, 00-diseno §7)
    |  Escanear / Detener / Enviar
    v
+----------------------------------------------------------+
| LogicaFake (telefono/LogicaFake.java, Java puro, sin pkg) |
|                                                          |
|  empezarEscaneo(cb) --tras retrasoDeteccionMs--> cb(null, codigoSimulado)
|  detenerEscaneo()   --cancela--> cb("deteccion detenida", null)
|  obtenerCodigo()    -> codigo:Texto | null (aún no hay)   |
|  enviarCodigo(codigo, cb)                                |
|        |                                                 |
|        +--> Transporte (interfaz)                        |
|               | por defecto: TransporteREST (GET ?codigo=)|
|               | en tests: transporte falso (simulado)     |
|               v                                          |
|          cb(error, VoF)                                  |
+----------------------------------------------------------+
```

- Detección **simulada** one-shot: al empezar, espera `retrasoDeteccionMs` y
  entrega `codigoSimulado`. No hay Bluetooth ni sensores reales.
- Envío por **transporte inyectable**: por defecto REST real; los tests lo sustituyen
  por un doble falso (envío simulado y errores simulados).

## 2. Aclaraciones en texto sobre el diseño

- **Por qué Java puro sin package**: para que los tests se compile y ejecute con
  solo `javac`/`java` (sin Android SDK ni emulador), cumpliendo "reproducibles y
  no dependen de dispositivos físicos". El flujo real beacon→app→REST del sistema
  queda representado por esta fake: la detección del beacon se simula y el envío
  REST es el mismo que usaría la app.
- Estado interno: `codigoSimulado` (defecto `"1234"`), `retrasoDeteccionMs`
  (defecto `1000`), `urlRest` (defecto `http://localhost:8080/rest/guardarCodigo.php`),
  `transporte` (defecto `TransporteREST`), `enEscaneo` (bool) y `codigoDetectado`.
- `empezarEscaneo`: devuelve `false` si ya hay un escaneo en curso; si había código
  detectado lo limpia. El callback de detección es **one-shot**: una sola invocación.
- `detenerEscaneo`: `true` si canceló un escaneo en curso (el `cb` pendiente recibe
  `("deteccion detenida", null)`); `false` si no había escaneo.
- `obtenerCodigo`: devuelve el código detectado o `null` si aún no hay
  (en notación: `codigo:Texto | error:Texto (null si no hay codigo)`).
- `enviarCodigo`: `false` + `cb("codigo invalido", false)` si el código no es válido
  (misma regla que la lógica: solo dígitos, 1-10, ≤ 2147483647) o si no hay código
  detectado; si no, `true` y delega en el transporte.
- `TransporteREST`: `HttpURLConnection` GET `<urlRest>?codigo=<codigo>`; éxito =
  HTTP 200 **y** el cuerpo contiene `"resultado":true`; cualquier otra cosa →
  `cb("fallo en la peticion REST", false)`. Sin hilos propios: se ejecuta en el
  hilo que llame (los tests lo llaman directamente).
- No hay UI en este fichero: la interfaz de 3 botones solo está diseñada
  (`00-diseno.md` §7) y su prompt no se entrega.

## 3. Directrices (qué y cómo exactamente)

1. Genera `telefono/LogicaFake.java`:
   - Declaración `public class LogicaFake` **sin package** e imports solo de
     `java.net.HttpURLConnection`, `java.net.URL`, `java.io.*`, `java.util.Timer`
     (o `ScheduledExecutorService` — uno de los dos, nada más).
   - Interfaces anidadas públicas:
     - `CallbackEscaneo { void callback(String error, String codigo); }`
     - `CallbackEnvio { void callback(String error, boolean resultado); }`
     - `Transporte { void enviar(String codigo, CallbackEnvio cb); }`
   - Constructores: `LogicaFake()` (defectos de la §2) y
     `LogicaFake(String codigoSimulado, long retrasoDeteccionMs, String urlRest)`.
   - `void setTransporte(Transporte t)`.
   - Métodos públicos: `boolean empezarEscaneo(CallbackEscaneo cb)`,
     `boolean detenerEscaneo()`, `String obtenerCodigo()`,
     `boolean enviarCodigo(String codigo, CallbackEnvio cb)`.
   - Clase interna `TransporteREST implements Transporte` con la semántica de la §2.
   - Clase interna `public static void main` **no**: nada de main (los tests son
     el `tests/TestLogicaFake.java` aparte).
2. Genera `tests/TestLogicaFake.java`: programa autoejecutable con `main`,
   sin JUnit ni frameworks. Helper `comprobar(boolean cond, String desc)` que
   imprime `[OK] desc` / `[FALLO] desc` en ASCII y lleva un contador; al final
   imprime el total y `System.exit(fallos > 0 ? 1 : 0)`.
   Para lo asíncrono usa `java.util.concurrent.CountDownLatch` con `await(timeout)`
   (nunca `Thread.sleep` como única sincronización).
3. Compilación y ejecución (deben funcionar tal cual):
   ```
   javac -encoding UTF-8 -d out telefono/LogicaFake.java tests/TestLogicaFake.java
   java -cp out TestLogicaFake
   ```

## 4. Comentarios exigidos (nuestra notación)

Cabecera de clase y de cada método público entre **dos paredes**, con la firma en
notación de la asignatura:

```java
//---------------------------------------------------------
//---------------------------------------------------------
//
// versión fake de la lógica del teléfono (sin dispositivo físico)
//
// -> empezarEscaneo() -> VoF
//   (detección simulada; resultado via callback( error:Texto, codigo:Texto ))
//
//---------------------------------------------------------
//---------------------------------------------------------
public boolean empezarEscaneo( CallbackEscaneo cb ) { ... }
```

Cabeceras exactas:

| Miembro            | Cabecera                                                        |
|--------------------|-----------------------------------------------------------------|
| clase              | `// versión fake de la lógica del teléfono (sin dispositivo físico)` |
| `empezarEscaneo`   | `// -> empezarEscaneo() -> VoF` + línea de callback             |
| `detenerEscaneo`   | `// -> detenerEscaneo() -> VoF`                                 |
| `obtenerCodigo`    | `// -> obtenerCodigo() -> codigo:Texto \| error:Texto` + `(null si no hay codigo)` |
| `enviarCodigo`     | `// codigo:Texto -> enviarCodigo() -> VoF` + línea de callback  |
| `Transporte`       | `// codigo:Texto -> transporte.enviar() -> VoF \| error:Texto (via callback)` |

Comentarios breves de finalidad (simulación de retardo, cancelación, validación,
llamada REST); **prohibido** comentar línea por línea.

## 5. Tests automáticos — `tests/TestLogicaFake.java`

Cubren los 4 puntos de la sección 7 "LÓGICA FAKE (SMARTPHONE)". Usar
`retrasoDeteccionMs` pequeño (p.ej. 50 ms) y latches con timeout para que el test
sea rápido y determinista; transporte falso inyectado con `setTransporte`:

1. **Detección simulada de beacon**: `empezarEscaneo(cb)` con `codigoSimulado="7777"`;
   el latch recibe `error=null, codigo="7777"`.
2. **Obtención del código**: tras la detección, `obtenerCodigo()` devuelve `"7777"`;
   antes de detectar devuelve `null`.
3. **Envío simulado**: transporte falso que responde éxito; `enviarCodigo("7777", cb)`
   → `cb(null, true)` y el transporte registró exactamente `"7777"`.
4. **Simulación de errores**:
   - `enviarCodigo("abc", ...)` → devuelve `false` y `cb("codigo invalido", false)`,
     sin llamar al transporte.
   - transporte falso que falla → `cb("fallo en la peticion REST", false)`.
   - `detenerEscaneo()` tras `empezarEscaneo` → `cb("deteccion detenida", null)`.
   - segundo `empezarEscaneo` en curso → devuelve `false`.
   - `obtenerCodigo()` sin detectar → `null`.

Sin Android, sin Bluetooth, sin red: todo con dobles inyectados.

## 6. Restricciones

- Solo la fake: no generes UI, ni `MainActivity`, ni escaneo BLE real
  (fuera de alcance de entrega; la UI solo está diseñada en `00-diseno.md` §7).
- Java 8 compatible, sin dependencias externas, sin package.
- No llames a la BD ni a PHP directamente: la fake solo habla por el `Transporte`.
- No añadas métodos más allá de los listados (no hay borrar, historial, etc.).
