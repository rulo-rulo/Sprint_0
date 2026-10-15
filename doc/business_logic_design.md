# Diseño del Componente: Lógica del Negocio (`business_logic_design.md`)

## 1. Diseño del Componente

La lógica del negocio valida y persiste el código detectado y recupera el último
código almacenado. Es **100% independiente del transporte**: no referencia HTTP,
rutas, cabeceras ni sockets. Es **consciente de la base de datos**: opera sobre la
tabla `CODIGO` definida en `database_design.md`.

Ubicación: `src/business_logic/`.

### Tipos

```
FactorResultado = { EXITO, ERROR }
```

### Funciones

```
codigo: Text --> esCodigoValido() --> B
```

Comprueba que `codigo` es una cadena no vacía, formada solo por dígitos, de
longitud 1..10 y con valor `<= 2147483647`.

```
codigo: Text --> guardarCodigo() --> B
```

Valida el código con `esCodigoValido()`; si es válido, inserta una fila en
`CODIGO (valor, fecha)` con la fecha actual y devuelve `true`. Si no es válido o
falla el acceso a datos, devuelve `false`.

```
--> obtenerUltimoCodigo() --> ( valor: Z, fecha: Text ) | error: Text
```

Devuelve el último registro de `CODIGO` (`ORDER BY id DESC LIMIT 1`) o un error
`"no hay ningun codigo guardado"` si la tabla está vacía.

## 2. Aclaraciones del Diseño

- La validación (`esCodigoValido`) es pura y no depende de la base de datos.
- `guardarCodigo` y `obtenerUltimoCodigo` acceden a la BD a través de
  `conexionBD()` (`src/database/conexionBD.php`), sin conocer el transporte.
- Los mensajes de error son cadenas de dominio:
  `"no hay ningun codigo guardado"`, `"error al conectar con la base de datos"`,
  `"error al consultar la base de datos"`.
- El resultado de `obtenerUltimoCodigo` se modela como agregación con un campo
  `error` alternativo, sin usar códigos de estado HTTP.

## 3. Reglas Generales

- **Lenguaje objetivo:** PHP.
- **Encabezados:** cada función incluye su diseño lógico en un bloque de
  comentarios delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios posibles.
- **Pruebas automatizadas:** `tests/testLogica.php` prueba validación,
  almacenamiento, consulta, formato de fecha y casos de error.

### Independencia de la comunicación (regla de cero dependencias)

Ningún fichero de `src/business_logic/` importa, invoca ni depende de
`src/communication/` ni de frameworks web. Esta regla se verifica de forma
automática con `tests/test_arquitectura.php`.
