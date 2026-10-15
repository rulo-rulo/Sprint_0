# Prompt 01 — Base de datos

> Prompt para generar base de datos. Objetivo: generar la base de datos del sistema descrito en
> diseño_de_proyecto.md` y `diseno_tarea1.md`. Respeta exactamente lo siguiente:
> no introduzcas nada que no haya pedido previamente.

---

## 1. Diseño gráfico del esquema

```
+-------------------------------+
|           C O D I G O         |
+-------------------------------+
| id     INT         PK, AUTO_INCREMENT |
| valor  INT         NOT NULL   |
| fecha  DATETIME    NOT NULL   |
+-------------------------------+
```

- Una única tabla: `CODIGO`.
- `id`: clave primaria, autoincremental (identifica cada código recibido).
- `valor`: el código del beacon (entero, obligatorio).
- `fecha`: fecha/hora de recepción en el servidor (obligatoria, sin DEFAULT:
  quien recibe el código la pone explícitamente al guardar).

## 2. Aclaraciones en texto sobre el diseño

- El sensor manda un código que el smartphone envía al servidor; la BD solo guarda
  cada código recibido con su fecha. No hay usuarios, sesiones ni más entidades.
- Nombre de la base de datos: `biometria` (sin acento). Tabla `CODIGO`.
- MySQL 8 con `utf8mb4` y modo estricto por defecto: una columna `NOT NULL` sin
  DEFAULT rechaza la inserción si falta — eso es lo que exige el test de restricciones.
- El script crea la BD si no existe, la selecciona y crea la tabla si no existe
  (idempotente: se puede ejecutar varias veces).
- `fecha` no lleva `DEFAULT CURRENT_TIMESTAMP`: la fecha de recepción la fija la
  lógica de negocio al guardar (decisión del diseño, sección 4 de `00-diseno.md`).

## 3. Directrices (qué y cómo exactamente)

1. Genera el fichero `bd/esquema.sql`.
2. Contenido, en este orden:
   - `CREATE DATABASE IF NOT EXISTS biometria CHARACTER SET utf8mb4;`
   - `USE biometria;`
   - `CREATE TABLE IF NOT EXISTS CODIGO ( ... );` con las 3 columnas del diseño,
     `id INT NOT NULL AUTO_INCREMENT PRIMARY KEY`, `valor INT NOT NULL`,
     `fecha DATETIME NOT NULL`, engine InnoDB.
3. Sin índices extra, sin FKs, sin triggers, sin datos de ejemplo.
4. Codificación UTF-8 del fichero; comentarios en español, breves.

## 4. Comentarios exigidos (nuestra notación)

- Cabecera del fichero entre **dos paredes** (`-- ---------` en dos líneas), con el
  propósito en una línea:

```sql
-- ---------------------------------------------------------
-- ---------------------------------------------------------
--
-- esquema de la base de datos: tabla CODIGO (id, valor, fecha)
--
-- ---------------------------------------------------------
-- ---------------------------------------------------------
```

- Un comentario `--` breve por delante de cada `CREATE` (BD, tabla). Nada más:
  prohibidos comentarios que describan línea por línea.

## 5. Tests automáticos — `tests/testBD.php`

Script PHP CLI autoejecutable (`php tests/testBD.php`), sin frameworks, que se
conecta a MySQL (`localhost`, `root`, vacía — mismos valores que usarán los demás
tests, pero declarados **al principio del propio test** con `define()`, para que
este prompt sea autocontenido y no dependa de otros prompts), ejecuta los
siguientes **4 grupos** y por cada comprobación
imprime `[OK] descripcion` o `[FALLO] descripcion` en ASCII (sin tildes); al final
`exit(1)` si hay algún FALLO, si no `exit(0)`.

1. **Creación de la tabla**: ejecuta `bd/esquema.sql` y verifica que la tabla
   `CODIGO` existe (`information_schema`).
2. **Inserción de un código**: inserta `(valor=1234, fecha='2026-01-01 10:00:00')`
   y verifica que `LAST_INSERT_ID()` > 0 y que la fila se recupera igual.
3. **Consulta de un código**: `SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1`
   devuelve la fila insertada.
4. **Restricciones** (cada una debe fallar; si no, `[FALLO]`):
   - `valor` a NULL → rechazado (NOT NULL).
   - `fecha` a NULL → rechazado (NOT NULL).
   - `valor` como texto no numérico (`'abc'`) → rechazado (modo estricto INT).
   - `id` duplicado explícito → rechazado (clave primaria).

El test debe ser **reproducible**: al empezar hace `DROP TABLE IF EXISTS CODIGO`
y vuelve a ejecutar `bd/esquema.sql`, de modo que siempre parte del mismo estado y
no depende de ningún dispositivo físico.

## 6. Restricciones

- Solo 1 fichero de esquema + 1 test. No generes PHP de lógica ni REST aquí.
- No añadas más tablas, columnas, índices o restricciones que los del diseño.
- Los tests no dependen de la red ni de sensores: solo de un MySQL local.
