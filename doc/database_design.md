# Diseño del Componente: Base de Datos (`database_design.md`)

## 1. Diseño del Componente

Base de datos relacional (MySQL / MariaDB) con una única tabla, `CODIGO`, que
almacena el código recibido del beacon (la medida ficticia del Sprint 0).

### Tipo lógico

```
CODIGO = [ ( id: N, valor: Z, fecha: Text ) ]
```

### Tabla `CODIGO`

```
====================================================================================
TABLE: CODIGO
DESCRIPTION: Almacena el ultimo codigo recibido del beacon y el instante de recepcion.

COLUMNS:

+ id    | INT      | NOT NULL | Auto-Increment
+ valor | INT      | NOT NULL
+ fecha | DATETIME | NOT NULL

PRIMARY KEY: id

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ CHECK (valor >= 0)
====================================================================================
```

El esquema SQL correspondiente se encuentra en `src/database/esquema.sql`.

## 2. Aclaraciones del Diseño

- `valor` es el código entero detectado por el teléfono (en el Sprint 0 siempre
  la medida ficticia `1005` devuelta por `Medidor.medirCO2()`).
- `fecha` registra el instante de recepción en zona horaria `Europe/Madrid`.
- `id` es autoincremental; el "último código" se obtiene con
  `ORDER BY id DESC LIMIT 1`.
- El modo SQL se fuerza a `STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION` para que
  la falta de valor o fecha provoque error (restricción NOT NULL efectiva).

## 3. Reglas Generales

- **Lenguaje objetivo:** SQL (MySQL / MariaDB).
- **Encabezados:** cada fichero y función incluye su diseño lógico en un bloque
  de comentarios delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios posibles.
- **Pruebas automatizadas:** se generan pruebas (`tests/testBD.php`) que crean la
  tabla, insertan, consultan y comprueban las restricciones.
