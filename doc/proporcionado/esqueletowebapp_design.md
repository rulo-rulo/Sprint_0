# Diseño Lógico - EsqueletoWebAppEnPHPConSesion (Web)

## Diagrama de clases (ASCII - PHP)

```
+---------------------------+
|  logica/diHola.php        |
+---------------------------+
| (funciones sueltas)       |
+---------------------------+
| + diHola(usuario: Texto): Objeto |
|   → {nombre: Texto, saludo: Texto} |
+---------------------------+

+---------------------------+
|  logica/hacerLogin.php    |
+---------------------------+
| (funciones sueltas)       |
+---------------------------+
| + hacerLogin(nombre: Texto, password: Texto): B |
|   → true si password == "1234"                 |
+---------------------------+

+---------------------------+
|  rest/diHola.php          |
+---------------------------+
| (endpoint REST)           |
+---------------------------+
| GET → verifica sesion     |
|   → llama diHola()        |
|   → retorna JSON          |
+---------------------------+

+---------------------------+
|  rest/hacerLogin.php      |
+---------------------------+
| (endpoint REST)           |
+---------------------------+
| GET ?nombre=&password=    |
|   → llama hacerLogin()    |
|   → guarda en sesion      |
|   → retorna JSON          |
+---------------------------+
```

## Diagrama de clases (ASCII - JavaScript)

```
+---------------------------+
|  ux/logicaFake/diHola.js  |
+---------------------------+
| + diHola(cb: Function): void |
|   → XMLHttpRequest GET    |
|   → ../rest/diHola.php    |
|   → cb(err, resultado)    |
+---------------------------+

+---------------------------+
| ux/logicaFake/hacerLogin.js|
+---------------------------+
| + hacerLogin(nombre, password, cb): void |
|   → XMLHttpRequest GET    |
|   → ../rest/hacerLogin.php?nombre=&password= |
|   → cb(resultado)         |
+---------------------------+
```

## Diagrama de componentes (ASCII)

```
+-------------------+     HTTP GET      +-------------------+
|  Aplicacion.html  | ───────────────→ |  rest/diHola.php  |
|  (navegador)      |                   |  (servidor PHP)   |
|                   |                   +-------------------+
|  diHola.js        |                         |
|  hacerLogin.js    |                         | require_once
+-------------------+                         v
                                        +-------------------+
                                        | logica/diHola.php |
                                        +-------------------+

+-------------------+     HTTP GET      +-------------------+
|  Aplicacion.html  | ───────────────→ |rest/hacerLogin.php|
|  (navegador)      |                   |  (servidor PHP)   |
|                   |                   +-------------------+
|                   |                         |
|                   |                         | require_once
|                   |                         v
|                   |                   +-------------------+
|                   |                   |logica/hacerLogin  |
|                   |                   +-------------------+
```

## Flujo de login

```
1. Usuario introduce nombre y password en formulario
2. loginPulsado() llama a hacerLogin(nombre, password, callback)
3. hacerLogin.js envia GET a rest/hacerLogin.php?nombre=X&password=Y
4. rest/hacerLogin.php:
   ├── session_start()
   ├── llama hacerLogin(nombre, password) de logica/
   ├── si true: $_SESSION["usuario"] = nombre, retorna {resultado:true}
   └── si false: session_destroy(), retorna {resultado:false}
5. Callback muestra resultado en pagina
```

## Flujo de diHola

```
1. Usuario pulsa "Llamar hola"
2. botonPulsado() llama a diHola(callback)
3. diHola.js envia GET a rest/diHola.php
4. rest/diHola.php:
   ├── session_start()
   ├── verifica $_SESSION["usuario"]
   ├── si no existe: retorna {error: "usuario no acreditado"}
   └── si existe: llama diHola(usuario), retorna {nombre, saludo, error:0}
5. Callback muestra saludo en pagina
```

## Seguridad

| Vulnerabilidad           | Ubicación                  | Severidad |
|--------------------------|----------------------------|-----------|
| Password hardcodeado     | logica/hacerLogin.php      | ALTA      |
| Credenciales por GET     | rest/hacerLogin.php        | ALTA      |
| Doble session_start()    | rest/hacerLogin.php:22,37  | MEDIA     |

## Tipos de datos

| Tipo   | Dominio   | Descripción                    |
|--------|-----------|--------------------------------|
| Texto  | String    | Nombre, password, saludo       |
| B      | Boolean   | Resultado del login            |
| Z      | Integer   | Codigo error                   |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
