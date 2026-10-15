# Prompt 05 — Lógica fake en el navegador

> Prompt para la IA. Objeto: generar la **lógica fake** que corre en el navegador
> (JavaScript), sistema descrito en diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente
> estas decisiones; no introduzcas funcionalidades no pedidas. Es la capa que la UX
> web (prompt 06) llama para consultar el último código al servidor.
> Dependencia funcional (no de generación): apunta a la ruta REST del prompt 03.

---

## 1. Diseño gráfico de la pieza

```
 ux/Aplicacion.html  (prompt 06)
    |
    |  obtenerUltimoCodigo( function(error, resultado){...} )
    v
+-------------------------------------------------------+
| ux/logicaFake/obtenerUltimoCodigo.js                  |
|                                                       |
|  XMLHttpRequest GET ../rest/obtenerUltimoCodigo.php   |
|                                                       |
|  status==200 y resultado=true  -> cb(null, {valor,fecha}) |
|  status==200 y resultado=false -> cb(error, null)   [ausencia] |
|  status!=200                   -> cb("fallo en la peticion REST", null) |
|  JSON ilegible                -> cb("respuesta invalida", null) |
+-------------------------------------------------------+
    |
    v  (red)
 rest/obtenerUltimoCodigo.php  (prompt 03)  ->  logica (02)  ->  MySQL
```

- **Una sola función fake**: `obtenerUltimoCodigo(cb)`. La web solo consulta
  (sección 3 de `instrucciones.md`: la web consulta y muestra; no guarda).
- Patrón idéntico al esqueleto del curso (`ux/logicaFake/diHola.js`):
  "versión fake de una función de la lógica" que llama *remotamente* al fichero
  REST (la verdadera función vive en `logica/`).

## 2. Aclaraciones en texto sobre el diseño

- **Callback con dos argumentos** `(error, resultado)` (estilo `diHola.js` del
  esqueleto): `error` es `null` si todo va bien, o `error:Texto` en caso contrario;
  `resultado` es el objeto `{valor:Int, fecha:Texto}` si hay datos, o `null` si no.
- Ausencia de código (tabla vacía → `{"resultado":false,"error":...}`) →
  `cb(<error del servidor>, null)`: para la UX es un caso de "no hay código",
  no un caso de red.
- Errores de transporte: cualquier `status != 200` →
  `cb("fallo en la peticion REST", null)`. Respuesta 200 pero cuerpo que no es
  JSON ni objeto esperado → `cb("respuesta invalida", null)`.
- La función no tiene estado: cada llamada lanza una petición nueva (la UX decide
  cuándo llamar; por defecto, al cargar la página).
- La URL es relativa al documento: `../rest/obtenerUltimoCodigo.php`
  (estilo esqueleto; el servidor se arranca en la raíz del proyecto).
- Sin promesas, sin `async/await`, sin `fetch`: se pide estilo esqueleto
  (XMLHttpRequest + callback) para mantener la notación de la asignatura.

## 3. Directrices (qué y cómo exactamente)

1. Genera `ux/logicaFake/obtenerUltimoCodigo.js`:
   - Define `function obtenerUltimoCodigo( cb ) { ... }` (global, sin módulos:
     nada de `module.exports` ni `export`; se carga con `<script src="...">`).
   - Dentro: `var xmlhttp = new XMLHttpRequest();` con `onreadystatechange` que
     comprueba `readyState == 4`:
     - `status == 200`: intenta `JSON.parse` del cuerpo en una variable
       `respuesta`; si lanza excepción → `cb("respuesta invalida", null)`.
       Si `respuesta.resultado === false` → `cb(respuesta.error, null)`
       (ausencia de código o error del servidor). Si `respuesta.resultado === true`
       → `cb(null, { valor: respuesta.valor, fecha: respuesta.fecha })`.
       Cualquier otra estructura → `cb("respuesta invalida", null)`.
     - `status != 200` → `cb("fallo en la peticion REST", null)`.
   - `xmlhttp.open("GET", "../rest/obtenerUltimoCodigo.php", true); xmlhttp.send();`
2. Sin parámetros de entrada (la función no recibe nada más que el callback).
3. Sin estilos, sin `console.log` (los tests capturan el callback directamente).
4. UTF-8, español en comentarios.

## 4. Comentarios exigidos (nuestra notación)

Cabecera entre **dos paredes** (estilo `diHola.js` del esqueleto):

```js
//---------------------------------------------------------
//---------------------------------------------------------
//
// versión fake de una función de la lógica
//
// -> obtenerUltimoCodigo() -> (valor:Int, fecha:Texto) | error:Texto
//
// (devuelto via callback( error:Texto, resultado ))
//
//---------------------------------------------------------
//---------------------------------------------------------
function obtenerUltimoCodigo( cb ) { ... }
```

Comentarios breves de finalidad: `// preparar la llamada remota`,
`// callback para cuando llegue la respuesta`,
`// llamamos *remotamente* al fichero obtenerUltimoCodigo.php (la verdadera
// función de la lógica)`. **Prohibido** comentar línea por línea
(máximo 3-4 comentarios en todo el fichero, como en el esqueleto).

## 5. Tests automáticos — `tests/testLogicaFakeNavegador.js`

Script Node.js (`node tests/testLogicaFakeNavegador.js`), sin frameworks
(solo módulos nativos `fs`, `path`, `vm`), autoejecutable: imprime
`[OK] desc` / `[FALLO] desc` en ASCII (sin tildes) y acaba con
`process.exit(1)` si hay fallos, `0` si todo pasa. Cubre los 3 puntos de la
sección 7 "LÓGICA FAKE (WEB)":

1. **Obtención de código**: carga el fichero fake en un `vm.createContext` con un
   `XMLHttpRequest` **falso** cuya respuesta simulada es
   `{"resultado":true,"valor":1234,"fecha":"2026-01-01 10:00:00"}` (status 200);
   comprueba que el callback recibe `error == null` y
   `resultado.valor == 1234`, `resultado.fecha` correcto, y que la URL pedida es
   `../rest/obtenerUltimoCodigo.php` con método GET.
2. **Ausencia de código**: respuesta 200 con
   `{"resultado":false,"error":"no hay ningun codigo guardado"}` →
   el callback recibe `error != null` y `resultado == null`.
3. **Simulación de error**:
   - status 500 → `cb("fallo en la peticion REST", null)`;
   - cuerpo 200 con JSON roto → `cb("respuesta invalida", null)`.

El XMLHttpRequest falso se define dentro del propio test (objeto con `open`,
`send`, `onreadystatechange` simulados que dispara `readyState=4` con el status
y cuerpo que el test indique): **sin red, sin navegador, sin instalar nada**.

## 6. Restricciones

- Solo `obtenerUltimoCodigo`: no generes `guardarCodigo` fake (la web no guarda;
  el flujo de `instrucciones.md` no lo permite).
- Sin frameworks, sin `fetch`/promesas, sin dependencias `npm`.
- No toques `logica/` ni `rest/` (prompts 02/03) ni el HTML (prompt 06):
  aquí solo la capa fake + su test.
- No añadas parámetros, callbacks extra ni opciones de configuración.
