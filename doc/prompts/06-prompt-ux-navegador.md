# Prompt 06 — UX en el navegador

> Prompt para la IA. Objeto: generar la interfaz gráfica web (HTML/CSS/JS) del
> sistema descrito en diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente
> estas decisiones; no introduzcas funcionalidades no pedidas. Sección 5
> instrucciones: *"La página web contará simplemente con un texto en mitad de la
> pantalla que muestre el código consultado al servidor."*
> Dependencia: carga `ux/logicaFake/obtenerUltimoCodigo.js` del prompt 05.

---

## 1. Diseño gráfico

```
+--------------------------------------+
|                                      |
|                                      |
|                 1234                 |   <- único texto: el código,
|                                      |      centrado en mitad de pantalla
|                                      |
+--------------------------------------+
   pantalla completa, fondo blanco, sin más elementos
```

- Un solo `<p>` (id `codigo`) centrado horizontal y verticalmente en el viewport
  (flexbox en `body`), tamaño de fuente legible (p.ej. `2rem`), color negro,
  fondo blanco, márgenes 0.
- Sin botones, sin cabecera, sin formulario, sin imágenes: solo el texto.

## 2. Aclaraciones en texto sobre el diseño

- Al **cargar la página** se consulta automáticamente el último código llamando a
  la lógica fake `obtenerUltimoCodigo(cb)` (prompt 05, incluida con `<script src>`).
- Éxito → el `<p>` muestra el valor del código (p.ej. `1234`), **solo el número**
  (sin "Código:", sin fecha: el enunciado pide "muestre el código").
- **No hay código o hay error** → el `<p>` queda **vacío** (estado inicial ya vacío).
  No se muestran mensajes de error: la especificación no los pide y no se añaden
  funcionalidades no especificadas.
- La página no vuelve a consultar sola: una consulta por carga (no hay polling
  especificado).
- Todo es estático: se sirve con `php -S localhost:8080` desde la raíz del
  proyecto y se carga `http://localhost:8080/ux/Aplicacion.html`
  (mismo esquema del `00-Leeme.txt` del esqueleto del curso).

## 3. Directrices (qué y cómo exactamente)

1. Genera `ux/Aplicacion.html` con la estructura del esqueleto del curso:
   `<!DOCTYPE html>`, `<html>`, `<head>` (con `<meta charset="utf-8"/>`), `<body>`.
2. En el `<head>`:
   - `<script src="logicaFake/obtenerUltimoCodigo.js"></script>` (prompt 05).
   - Un `<script>` con `function consultaInicial()` que llama
     `obtenerUltimoCodigo( function( error, resultado ) { ... } )`:
     si `error` → no hace nada (texto vacío); si no →
     `document.getElementById("codigo").innerHTML = resultado.valor;`.
   - La llamada se dispara con `window.onload = consultaInicial;`.
3. En el `<body>`: únicamente
   `<p id="codigo"></p>` (vacío por defecto) + el CSS.
4. CSS (en `<style>` del `<head>` o atributos, lo más simple):
   `html, body { height:100%; }`, `body { margin:0; display:flex;
   justify-content:center; align-items:center; background:#fff; color:#000;
   font-family: sans-serif; }`, `#codigo { font-size:2rem; }`.
5. Sin frameworks, sin librerías externas, sin CDN: HTML/CSS/JS puro.
6. UTF-8, español en comentarios.

## 4. Comentarios exigidos (nuestra notación)

**Dos paredes** (`<!-- ---------------------- -->` en dos líneas) separando las
partes, exactamente como `Aplicacion.html` del esqueleto:

```html
<!-- ---------------------------------------------------- -->
<!-- ---------------------------------------------------- -->
<!DOCTYPE html>
<html>

<!-- ---------------------------------------------------- -->
<!-- ---------------------------------------------------- -->
<head>
<meta charset="utf-8"/>

<!-- ---------------------------------------------------- -->
<!-- carga de la lógica fake y script de la consulta -->
<script src="logicaFake/obtenerUltimoCodigo.js"></script>

<!-- ---------------------------------------------------- -->
<!-- consulta el último código al cargar y lo muestra -->
<script>
function consultaInicial() { ... }
</script>
```

Comentarios breves de finalidad (qué hace cada bloque: CSS, script de consulta,
etiqueta de salida). **Prohibido** comentar línea por línea; el script debe tener
como mucho 2 comentarios.

## 5. Tests automáticos — `tests/testUx.js`

La sección 7 de `instrucciones.md` **no enumera tests para la UX**; para que esta
parte quede también verificable de forma reproducible se pide un test
estructural mínimo (sin navegador, sin red):

Script Node.js (`node tests/testUx.js`), sin frameworks, que lee
`ux/Aplicacion.html` como texto y comprueba (`[OK]`/`[FALLO]` ASCII, `exit(1)`
si hay fallos):

1. Existe el elemento `<p id="codigo">` y está vacío.
2. Se incluye `<script src="logicaFake/obtenerUltimoCodigo.js">` y el HTML llama
   a `obtenerUltimoCodigo(`.
3. El CSS centra el contenido (aparecen `justify-content:center` y
   `align-content`/`align-items:center`) y hay `font-size` para el texto.
4. No hay elementos no especificados: el test falla si detecta `<button`,
   `<form` o `<input` (la web no tiene botones según la sección 5).

## 6. Restricciones

- Solo 1 fichero de UI (+ su test estructural). No generes lógica fake aquí
  (prompt 05) ni rutas REST ni BD.
- No añadas: botones, formularios, campos, imágenes, íconos, menús, pie de
  página, metadatos OpenGraph, refresh automático ni mensajes de error.
- El texto muestra **solo el valor** del código; nada más.
