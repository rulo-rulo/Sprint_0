# Sprint 0 — Proyecto de Biometría y Medio Ambiente

Autor: **Raúl Sanabria Rodado**

Sprint 0 del proyecto: una medida ficticia se introduce en el código de la placa
**Arduino (C++)**, se envía al **teléfono Android (Java)**, pasa por el **API REST**
y la **lógica del negocio** hasta la **base de datos**, y una **página web** muestra
la última medida almacenada.

## Arquitectura

Arquitectura en capas con separación estricta de responsabilidades
(`communication -> business_logic -> database`):

```
Arduino (emite iBeacon)
        |  BLE 2.4 GHz
        v
Android (escanea y detecta el codigo)  -->  GUI web (muestra el codigo)
        |  HTTP GET (REST)
        v
src/communication/   (API REST)
        |
        v
src/business_logic/  (validacion + persistencia; sin HTTP)
        |
        v
src/database/        (PDO MySQL; tabla CODIGO)
```

## Estructura del repositorio

```
author.md                     nombre del autor
README.md                     este fichero
index.html                    portada raiz (redirige a la app)
doc/                          documentacion de diseño
  communication_design.md
  business_logic_design.md
  database_design.md
  frontend_business_logic_design.md
  prompts/                    prompts de IA usados (01..06)
  proporcionado/              ingenieria inversa del codigo proporcionado
src/
  communication/              API REST (PHP)
  business_logic/             logica del negocio (PHP)
  database/                   conexion, configuracion y esquema SQL
  frontend_business_logic/    logica "fake" del cliente (JS y Java)
  gui/                        interfaz web
  arduino/                    firmware del beacon (C++)
  android/                    aplicacion Android (Java)
tests/                        pruebas automaticas
```

## Despliegue

1. Copiar el proyecto al servidor (por ejemplo, bajo el directorio `Sprint_0/`).
2. Crear la base de datos y la tabla aplicando `src/database/esquema.sql`.
3. Copiar `src/database/configBD.sample.php` a `src/database/configBD.php` y
   rellenar los valores reales de conexion (servidor, puerto, usuario, clave, BD).
   El fichero `configBD.php` **no** se versiona (esta en `.gitignore`).
4. La web queda accesible en `.../Sprint_0/` (redirige a `src/gui/Aplicacion.html`).

## Como ejecutar los tests

Requisitos: PHP con PDO MySQL, Node.js y un JDK.

```
# Todo de una vez
tests/run_all.ps1

# Individualmente
php  tests/testBD.php          # esquema, insercion, consulta y restricciones
php  tests/testLogica.php      # logica del negocio
php  tests/testREST.php        # API REST
php  tests/test_arquitectura.php  # la logica no depende de la comunicacion
node tests/testLogicaFakeNavegador.js
node tests/testUx.js
javac -d out src/frontend_business_logic/LogicaFake.java tests/TestLogicaFake.java
java  -cp out TestLogicaFake
```

## Criterios de aceptación

- **Base de datos:** la tabla `CODIGO` se crea y admite inserciones validas;
  `valor` y `fecha` son obligatorios (`tests/testBD.php`).
- **Logica del negocio:** valida, guarda y recupera el ultimo codigo, e informa
  de la ausencia de datos (`tests/testLogica.php`).
- **API REST:** devuelve el JSON esperado en exito y en error (`tests/testREST.php`).
- **Arquitectura:** `src/business_logic/` no depende de `src/communication/`
  (`tests/test_arquitectura.php`).
- **Frontend:** la logica fake del navegador y del telefono se comporta segun el
  contrato (`tests/testLogicaFakeNavegador.js`, `tests/TestLogicaFake.java`).
- **Interfaz:** la web contiene las piezas necesarias y muestra el codigo
  (`tests/testUx.js`).
- **Test de funcionamiento:** la medida ficticia `1005` recorrida de extremo a
  extremo aparece en la web.

## Uso de herramientas IA

Los prompts detallados usados para generar cada componente estan en
`doc/prompts/`. El diseño de cada componente se valido contra su implementacion.
