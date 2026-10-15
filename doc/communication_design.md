# Diseño del Componente: Comunicación REST (`communication_design.md`)

## 1. Diseño del Componente

Capa de comunicación que expone la lógica del negocio como un API REST (HTTP GET)
y traduce peticiones/respuestas a JSON. Ubicación: `src/communication/`.

La comunicación **invoca** la lógica del negocio; nunca al contrario.

### Tipos de respuesta

```
RespuestaGuardar = ( resultado: B, error: Text )
RespuestaUltimo  = ( resultado: B, valor: Z, fecha: Text, error: Text )
```

### Endpoints

```
codigo: Text --> GET guardarCodigo.php --> RespuestaGuardar
```
- Entrada: parámetro de consulta `codigo`.
- Respuesta `200`:
  - Éxito: `{ "resultado": true }`
  - Código inválido: `{ "resultado": false, "error": "codigo invalido" }`
  - Error de BD: `{ "resultado": false, "error": "error al guardar el codigo" }`

```
--> GET obtenerUltimoCodigo.php --> RespuestaUltimo
```
- Respuesta `200`:
  - Éxito: `{ "resultado": true, "valor": <Int>, "fecha": "<Text>" }`
  - Sin datos: `{ "resultado": false, "error": "no hay ningun codigo guardado" }`

### Orquestación

1. `guardarCodigo.php`: lee `$_GET['codigo']`, llama a `esCodigoValido()` y
   `guardarCodigo()` (lógica del negocio) y compone el JSON.
2. `obtenerUltimoCodigo.php`: llama a `obtenerUltimoCodigo()` y compone el JSON.

## 2. Aclaraciones del Diseño

- El contrato es intencionadamente simple (GET + JSON) para el Sprint 0.
- Los códigos de estado HTTP no forman parte de la lógica del negocio; aquí solo
  se emite `200` con un cuerpo JSON que indica el resultado.
- El componente de comunicación es el único que conoce `$_GET` y `json_encode`.

## 3. Reglas Generales

- **Lenguaje objetivo:** PHP.
- **Encabezados:** cada fichero y función incluye su diseño lógico en un bloque de
  comentarios delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios posibles.
- **Pruebas automatizadas:** `tests/testREST.php` invoca los endpoints con `$_GET`
  simulado y comprueba el JSON devuelto.
