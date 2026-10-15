# Prompt 03 — Servidor REST (backend)

> Prompt para la IA. Objeto: generar el servidor REST del backend (PHP) del sistema descrito en
> diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente estas
> decisiones; no introduzcas rutas, métodos ni funcionalidades no pedidos.
> Dependencias: requiere `logica/` (prompt 02) y `bd/esquema.sql` (prompt 01).

---

## 1. Diseño gráfico de las rutas

```
 cliente (web o teléfono)
    |
    |  GET ../rest/guardarCodigo.php?codigo=<Texto>
    |        -> { "resultado": VoF [, "error": Texto] }
    |
    |  GET ../rest/obtenerUltimoCodigo.php
    |        -> { "resultado": true, "valor": Int, "fecha": Texto }
    |         | { "resultado": false, "error": Texto }
    v
 rest/*.php  ──require──>  logica/*.php  ──>  MySQL (CODIGO)
```

| Método | Ruta                              | Parámetros       | Respuesta JSON                                                |
|--------|-----------------------------------|------------------|---------------------------------------------------------------|
| GET    | `../rest/guardarCodigo.php`       | `codigo=<Texto>` | `{"resultado":VoF}`; si falla `{"resultado":false,"error":"codigo invalido"}` o `"error al guardar el codigo"` |
| GET    | `../rest/obtenerUltimoCodigo.php` | ninguno          | `{"resultado":true,"valor":Int,"fecha":Texto}` o `{"resultado":false,"error":Texto}` |

## 2. Aclaraciones en texto sobre el diseño

- Las rutas son **meros traductores**: leen `$_GET`, llaman a la lógica real del
  prompt 02 (`require_once('../logica/...')`) y devuelven `json_encode`. **Nunca**
  reimplementan la validación ni las consultas (la lógica "verdadera" vive en
  `logica/`, según la arquitectura SSD).
- Ambas rutas son GET con parámetros en la query (estilo del esqueleto del curso,
  `rest/hacerLogin.php`). No hay POST, PUT, DELETE, cabeceras especiales ni CORS.
- Forma de la respuesta: siempre un objeto JSON con campo `resultado` (VoF);
  el campo `error:Texto` solo cuando `resultado=false`. Cadenas de error fijas:
  `"codigo invalido"`, `"error al guardar el codigo"`, y las que devuelva la lógica
  (`"no hay ningun codigo guardado"`, ...).
- Sin sesiones, sin autenticación, sin parámetros extra: no está especificado.
- El cuerpo de la respuesta sale con `echo json_encode(...)` (estilo esqueleto).

## 3. Directrices (qué y cómo exactamente)

1. Genera en `rest/`:
   - `guardarCodigo.php` — recibe `$_GET["codigo"] ?? ""`, llama a
     `guardarCodigo($codigo)`, responde `{"resultado":true}` o
     `{"resultado":false,"error":"error al guardar el codigo"}`.
     (La distinción `"codigo invalido"` se hace si la lógica expone
     `esCodigoValido`; en ese caso se responde ese error antes de guardar.
     Decisión fija: usar `esCodigoValido` para el mensaje y `guardarCodigo`
     para la inserción.)
   - `obtenerUltimoCodigo.php` — llama a `obtenerUltimoCodigo()`; si el objeto
     traída tiene `error` → `{"resultado":false,"error":<ese>}`, si no →
     `{"resultado":true,"valor":...,"fecha":...}`.
2. Estructura de cada fichero (idéntica a la del esqueleto):
   `<?php` → dos paredes + cabecera en notación → `require_once` de la lógica →
   lectura de `$_GET` → llamada a la lógica → `echo json_encode(...)` → fin.
3. Sin `session_start`, sin `header()`s, sin procesamiento de errores nuevo:
   mínimo imprescindible.
4. UTF-8, español en comentarios.

## 4. Comentarios exigidos (nuestra notación)

Cabecera de cada fichero REST entre dos paredes, con la firma HTTP en notación:

```php
//---------------------------------------------------------
//---------------------------------------------------------
//
// GET ../rest/guardarCodigo.php?codigo=<Texto>
//
// @return
//  resultado:VoF
//  error:Texto   (solo cuando resultado=false)
//
//---------------------------------------------------------
//---------------------------------------------------------
```

Y el mismo patrón para `obtenerUltimoCodigo.php`:

```php
// GET ../rest/obtenerUltimoCodigo.php
//
// @return
//  resultado:VoF, valor:Int, fecha:Texto   (si resultado=true)
//  resultado:VoF, error:Texto              (si resultado=false)
```

Comentario breve antes de la llamada a la lógica (`// llamo a la función de la
lógica (la verdadera)`). **Prohibido** comentar línea por línea.

## 5. Tests automáticos — `tests/testREST.php`

Script PHP CLI (`php tests/testREST.php`), sin frameworks, **sin red ni servidor
HTTP**: configura `$_GET`, hace `ob_start()` + `include` del fichero REST y captura
el JSON impreso; verifica con `json_decode`. Imprime `[OK]`/`[FALLO]` en ASCII y
`exit(1)` si hay fallos. Estado reproducible: al empezar ejecuta
`DROP TABLE IF EXISTS CODIGO` + `bd/esquema.sql`.

Comprobaciones:

1. `guardarCodigo.php?codigo=1234` → `{"resultado":true}` y la fila existe.
2. `guardarCodigo.php?codigo=abc` → `resultado=false` con `error` de los fijos.
3. `obtenerUltimoCodigo.php` (con datos) → `resultado=true`, `valor=1234`,
   `fecha` con formato `Y-m-d H:i:s`.
4. `obtenerUltimoCodigo.php` (tabla vacía) → `resultado=false`, `error` presente.

Cobertura: humo de las 2 rutas (la sección 7 no enumera tests de REST; este smoke
test cubre la capa sin dependencias físicas).

## 6. Restricciones

- Solo las 2 rutas listadas. No crees más endpoints, métodos ni versionado.
- No dupliques lógica de negocio: todo pasa por `logica/`.
- No generes aquí lógica, HTML ni código de teléfono (prompts 02/04/06).
