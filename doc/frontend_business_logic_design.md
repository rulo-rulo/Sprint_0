# Diseño del Componente: Lógica del Negocio del Cliente (`frontend_business_logic_design.md`)

## 1. Diseño del Componente

Componente de lógica del negocio en el lado cliente que presenta a la GUI la
**misma interfaz de dominio** que la lógica del negocio del backend
(`business_logic_design.md`). Por dentro actúa como un **proxy/fake** que
encapsula las llamadas de comunicación al backend. La GUI nunca realiza
HTTP directamente; solo invoca estas funciones de dominio.

Ubicación: `src/frontend_business_logic/`.

### Interfaz de dominio (idéntica a la lógica del negocio del backend)

Las firmas coinciden exactamente con las de `business_logic_design.md`:

```
codigo: Text --> esCodigoValido() --> B
```

```
codigo: Text --> guardarCodigo() --> B
```

```
--> obtenerUltimoCodigo() --> ( valor: Z, fecha: Text ) | error: Text
```

### Implementación por cliente (proxy encapsulado)

- **Navegador (JavaScript, `obtenerUltimoCodigo.js`):**
  implementa `obtenerUltimoCodigo()` mediante `XMLHttpRequest` contra
  `../communication/obtenerUltimoCodigo.php` y entrega el resultado por callback
  `( error: Text, resultado )`.
- **Teléfono (Java, `LogicaFake.java`):**
  implementa `esCodigoValido()` (misma regla de validación) y `guardarCodigo()`
  (implementado como `enviarCodigo()`), delega en un `Transporte` inyectable
  (`TransporteREST` real o `TransporteFalso` para pruebas).

### Capa de dispositivo (Android), fuera del proxy

El teléfono necesita además detectar el código del beacon. Estas operaciones de
dispositivo pertenecen a la capa GUI/dispositivo y **no** forman parte de la
interfaz de dominio (no tienen contrapartida en la lógica del negocio del
backend):

```
--> empezarEscaneo() --> B
--> detenerEscaneo() --> B
--> obtenerCodigo() --> Text | error: Text
```

## 2. Aclaraciones del Diseño

- Se aplica la **regla de subconjunto**: el proxy solo expone las funciones de
  dominio que realmente utiliza la GUI (`esCodigoValido`, `guardarCodigo`,
  `obtenerUltimoCodigo`).
- La **capa de dispositivo** (detección del beacon) se documenta aparte porque no
  es una función de dominio del backend; su origen es hardware/BLE.
- La GUI queda desacoplada del transporte: el proxy es el único punto que conoce
  la URL y el JSON.

## 3. Reglas Generales

- **Lenguaje objetivo:** JavaScript (navegador) y Java (teléfono Android).
- **Encabezados:** cada fichero y función incluye su diseño lógico en un bloque de
  comentarios delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios posibles.
- **Pruebas automatizadas:** `tests/testLogicaFakeNavegador.js` (JS) y
  `tests/TestLogicaFake.java` (Java, con transporte falso).
