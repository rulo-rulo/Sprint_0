# Prompt 02 — Lógica de negocio real (backend)

> Prompt para la logica. Objeto: generar la lógica de negocio **verdadera** del backend
> (PHP) del sistema descrito en diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente lo siguiente:
> no introduzcas nada que no haya pedido previamente.
> Dependencia: su test usa `bd/esquema.sql` ya generado por el prompt 01.

---

## 1. Diseño gráfico de la pieza

```
   guardarCodigo(codigo:Texto)            obtenerUltimoCodigo()
            |                                     |
            v                                     v
  +-------------------+               +----------------------+
  | esCodigoValido()  |--false--> VoF | SELECT ORDER BY id   |
  +-------------------+               | DESC LIMIT 1         |
            |true                     +----------------------+
            v                              |            |
   INSERT (valor, fecha=ahora)      {valor,fecha}   {error:Texto}
            |                                |            |
            v                                +------------+
          VoF                                     |
   (false si PDOException)                        v
                                         llamada por REST (prompt 03)
```

- `logica/` es el corazón: **solo** funciones puras de negocio + acceso a BD.
- Las rutas REST (prompt 03) son meros traductores HTTP→lógica.

## 2. Aclaraciones en texto sobre el diseño

- **Regla de validez (decisión fija)**: un código es válido si y solo si es una
  cadena no vacía, **solo dígitos** (`ctype_digit`), de 1 a 10 caracteres, y su valor
  numérico ≤ 2147483647 (rango del `INT` de la BD).
  Válidos: `"0"`, `"42"`, `"007"`. Inválidos: `""`, `"abc"`, `"-5"`, `"12a"`,
  `" 12"`, `"12345678901"`. Si es inválido **no se inserta nada**.
- **Fecha de recepción**: la pone el servidor al guardar: `date('Y-m-d H:i:s')`
  con zona horaria `Europe/Madrid` fijada en `configBD.php`. No se acepta fecha
  del cliente.
- `guardarCodigo` devuelve solo `VoF`; `obtenerUltimoCodigo` devuelve
  `(valor, fecha)` o `error:Texto` si la tabla está vacía o hay fallo de BD
  (cadenas de error fijas: `"no hay ningun codigo guardado"`).
- Conexión PDO con `ERRMODE_EXCEPTION` y consultas **preparadas** siempre
  (nunca concatenar valores). Constantes de conexión en un único sitio
  (`logica/configBD.php`): `localhost`, `root`, vacía, BD `biometria`.
- Esta lógica es la que usarán los tests de negocio; el REST y la web la invocan
  remotamente, nunca reescriben estas reglas.

## 3. Directrices (qué y cómo exactamente)

1. Genera en `logica/`:
   - `configBD.php` — constantes (servidor, usuario, clave, BD, timezone) +
     `date_default_timezone_set('Europe/Madrid')`. Sin función de negocio.
   - `conexionBD.php` — `-> conexionBD() -> conexion:Conexion | error:Texto`:
     devuelve `PDO` conectado; captura excepción y devuelve `error:Texto`.
   - `esCodigoValido.php` — regla de la sección 2, devuelve `VoF`.
   - `guardarCodigo.php` — valida con `esCodigoValido`; si inválido `false`;
     si válido `INSERT INTO CODIGO (valor, fecha) VALUES (?, ?)` con
     `(int)$codigo` y `date('Y-m-d H:i:s')` → `true`; `PDOException` → `false`.
   - `obtenerUltimoCodigo.php` — `SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1`;
     sin filas → objeto `{error:"no hay ningun codigo guardado"}`;
     `PDOException` → `{error: ...}`; si no, `stdClass {valor:Int, fecha:Texto}`.
2. Un fichero por función, `require_once` de lo que usen (p.ej. `guardarCodigo`
   requiere `esCodigoValido` y `conexionBD`).
3. Devoluciones tipo `stdClass` (estilo del esqueleto del curso). Nada de clases,
   frameworks, ORMs ni PDO alternativo.
4. Siempre `<?php ... ?>` con los ficheros en UTF-8.

## 4. Comentarios exigidos (nuestra notación)

Cabecera de **cada función** entre dos paredes, con su firma en la notación de la
asignatura y una línea de finalidad:

```php
//---------------------------------------------------------
//---------------------------------------------------------
//
// codigo:Texto -> guardarCodigo() -> VoF
//
// valida el código y lo inserta en la BD con la fecha de recepción
//
//---------------------------------------------------------
//---------------------------------------------------------
function guardarCodigo( $codigo ) { ... }
```

Cabeceras exactas a usar:

| Fichero                | Cabecera                                                        |
|------------------------|-----------------------------------------------------------------|
| `configBD.php`         | `// constantes de conexión y zona horaria de la lógica`         |
| `conexionBD.php`       | `// -> conexionBD() -> conexion:Conexion \| error:Texto`       |
| `esCodigoValido.php`   | `// codigo:Texto -> esCodigoValido() -> VoF`                    |
| `guardarCodigo.php`    | `// codigo:Texto -> guardarCodigo() -> VoF`                     |
| `obtenerUltimoCodigo.php` | `// -> obtenerUltimoCodigo() -> (valor:Int, fecha:Texto) \| error:Texto` |

Comentarios breves de finalidad en bloques (validación, inserción, consulta);
**prohibido** comentar línea por línea. Cada fichero abre con dos paredes +
propósito del fichero.

## 5. Tests automáticos — `tests/testLogica.php`

Script PHP CLI (`php tests/testLogica.php`), sin frameworks, autoejecutable:
al empezar `DROP TABLE IF EXISTS CODIGO` + ejecuta `bd/esquema.sql` (estado
reproducible), imprime `[OK]`/`[FALLO]` en ASCII por comprobación y `exit(1)`
si hay fallos. Cobre los 5 puntos de la sección 7 "LÓGICA DE NEGOCIO":

1. **Recepción de un código válido**: `guardarCodigo("1234") === true`.
2. **Rechazo de un código inválido**: `esCodigoValido`/`guardarCodigo` con
   `"abc"`, `""`, `"-5"`, `"12a"`, `"12345678901"` → `false`, y verificar que
   **no se insertó ninguna fila** (conteo antes/después).
3. **Almacenamiento correcto**: tras `guardarCodigo("42")`, la BD contiene
   `valor=42`.
4. **Consulta correcta**: `obtenerUltimoCodigo()` devuelve `valor=42`;
   con la tabla vacía devuelve `error` = `"no hay ningun codigo guardado"`.
5. **Registro de la fecha de recepción**: tras guardar, `fecha` del registro
   tiene formato `Y-m-d H:i:s` y está dentro de ±5 minutos de la hora actual.

Sin red, sin sensores: solo MySQL local.

## 6. Restricciones

- No generes REST, HTML ni código de teléfono aquí (prompts 03/04/06).
- No añadas operaciones de negocio más allá de guardar y consultar el último
  código (no hay búsquedas por id, borrados, usuarios ni sesiones).
- No expongas excepciones sin capturar: la lógica devuelve `VoF` o `error:Texto`.
