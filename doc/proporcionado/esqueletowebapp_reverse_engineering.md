# Ingeniería Inversa - EsqueletoWebAppEnPHPConSesion

## 1. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        SISTEMA WEB CON SESIONES                         │
│                                                                         │
│  CAPA DE PRESENTACIÓN (Frontend)                                        │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  NAVEGADOR WEB                                                  │    │
│  │  ┌──────────────────────────────────────────────────────────┐   │    │
│  │  │  Aplicacion.html                                         │   │    │
│  │  │  - Formulario: nombre + password                         │   │    │
│  │  │  - Botón "Log in" → loginPulsado()                       │   │    │
│  │  │  - Botón "Llamar hola" → botonPulsado()                  │   │    │
│  │  │  - Area de salida (p#salida)                             │   │    │
│  │  └──────────────────────────────────────────────────────────┘   │    │
│  │                          │                                      │    │
│  │  ┌───────────────────────┴──────────────────────────────────┐   │    │
│  │  │  logicaFake/                                             │   │    │
│  │  │  ├── hacerLogin.js  (XMLHttpRequest → POST)              │   │    │
│  │  │  └── diHola.js      (XMLHttpRequest → GET)               │   │    │
│  │  └──────────────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                    │
│                              HTTP (AJAX)                                │
│                                    │                                    │
│  CAPA DE NEGOCIO (Backend)                                             │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  SERVIDOR PHP                                                   │    │
│  │                                                                 │    │
│  │  CAPA REST (Endpoints)                                          │    │
│  │  ┌──────────────────────┐  ┌──────────────────────────────┐    │    │
│  │  │ rest/hacerLogin.php  │  │ rest/diHola.php               │    │    │
│  │  │ POST → $_POST[]      │  │ GET → $_SESSION["usuario"]    │    │    │
│  │  └──────────┬───────────┘  └──────────────┬───────────────┘    │    │
│  │             │                              │                    │    │
│  │  CAPA LÓGICA (Business Logic)                               │    │
│  │  ┌──────────┴───────────┐  ┌──────────────┴───────────────┐    │    │
│  │  │logica/hacerLogin.php │  │ logica/diHola.php             │    │    │
│  │  │ password_verify()    │  │ diHola(usuario) → {nombre,    │    │    │
│  │  └──────────────────────┘  │                 saludo}       │    │    │
│  │                            └──────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                    │
│  CAPA DE DATOS (Sesiones)                                               │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  PHP SESSION                                                    │    │
│  │  $_SESSION["usuario"] = "nombre_del_usuario"                    │    │
│  │  (almacenada en servidor, cookie PHPSESSID en navegador)        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2. Diagrama de Componentes

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│   ┌─────────────┐     POST      ┌──────────────────┐                │
│   │  hacerLogin │──────────────→│ rest/hacerLogin  │                │
│   │  .js        │  nombre,pass  │ .php             │                │
│   └─────────────┘               └────────┬─────────┘                │
│                                          │                           │
│                                          │ require_once              │
│                                          ▼                           │
│                                 ┌──────────────────┐                │
│                                 │logica/hacerLogin │                │
│                                 │ .php             │                │
│                                 │                  │                │
│                                 │ password_verify()│                │
│                                 └────────┬─────────┘                │
│                                          │                           │
│                                          │ return true/false         │
│                                          ▼                           │
│   ┌─────────────┐  JSON   ┌──────────────────┐                     │
│   │ Aplicacion  │←────────│ rest/hacerLogin  │                     │
│   │ .html       │ {result}│ .php             │                     │
│   │             │         └──────────────────┘                     │
│   │             │                                                    │
│   │             │   GET    ┌──────────────────┐                     │
│   │  diHola  ───│─────────→│ rest/diHola.php  │                     │
│   │  .js        │          └────────┬─────────┘                     │
│   └─────────────┘                   │                                │
│                                     │ require_once                   │
│                                     ▼                                │
│                            ┌──────────────────┐                     │
│                            │logica/diHola.php │                     │
│                            │                  │                     │
│                            │ diHola(usuario)  │                     │
│                            └────────┬─────────┘                     │
│                                     │                                │
│                                     │ return {nombre, saludo}        │
│                                     ▼                                │
│   ┌─────────────┐  JSON   ┌──────────────────┐                     │
│   │ Aplicacion  │←────────│ rest/diHola.php  │                     │
│   │ .html       │ {nombre,│                  │                     │
│   │             │  saludo}└──────────────────┘                     │
│   └─────────────┘                                                    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

## 3. Diagrama de Secuencia - Login

```
  NAVEGADOR                 SERVIDOR PHP               SESIÓN
     │                          │                        │
     │  1. Usuario escribe      │                        │
     │     nombre y password    │                        │
     │                          │                        │
     │  2. loginPulsado()       │                        │
     │     │                    │                        │
     │     │ 3. hacerLogin()    │                        │
     │     │    (JS)            │                        │
     │     │    │               │                        │
     │     │    └──→ XMLHttpRequest                      │
     │     │         POST       │                        │
     │     │  ─────────────────→│                        │
     │     │     nombre=Pepe    │                        │
     │     │     &password=xyz  │                        │
     │     │                    │                        │
     │     │                    │ 4. session_start()     │
     │     │                    │  ─────────────────────→│
     │     │                    │                        │
     │     │                    │ 5. $_POST["nombre"]    │
     │     │                    │    $_POST["password"]  │
     │     │                    │                        │
     │     │                    │ 6. hacerLogin()        │
     │     │                    │    (PHP)               │
     │     │                    │    │                   │
     │     │                    │    └──→ password_verify│
     │     │                    │         ("xyz", hash)  │
     │     │                    │         → return true  │
     │     │                    │                        │
     │     │                    │ 7. $_SESSION["usuario"]│
     │     │                    │    = "Pepe"            │
     │     │                    │  ─────────────────────→│
     │     │                    │                        │
     │     │                    │ 8. json_encode()       │
     │     │  ←─────────────────│ {resultado:true,       │
     │     │  JSON response     │  usuario:"Pepe"}       │
     │     │                    │                        │
     │  9. callback()           │                        │
     │     │ Mostrar "login ok" │                        │
     │     ▼                    │                        │
```

## 4. Diagrama de Secuencia - diHola

```
  NAVEGADOR                 SERVIDOR PHP               SESIÓN
     │                          │                        │
     │  1. Usuario pulsa        │                        │
     │     "Llamar hola"        │                        │
     │                          │                        │
     │  2. botonPulsado()       │                        │
     │     │                    │                        │
     │     │ 3. diHola() (JS)   │                        │
     │     │    │               │                        │
     │     │    └──→ XMLHttpRequest                      │
     │     │         GET        │                        │
     │     │  ─────────────────→│                        │
     │     │                    │                        │
     │     │                    │ 4. session_start()     │
     │     │                    │  ─────────────────────→│
     │     │                    │                        │
     │     │                    │ 5. $_SESSION["usuario"]│
     │     │                    │    = "Pepe" ←──────────│
     │     │                    │                        │
     │     │                    │ 6. diHola("Pepe")      │
     │     │                    │    (PHP)               │
     │     │                    │    │                   │
     │     │                    │    └──→ {nombre:"Pepe",│
     │     │                    │           saludo:"..."}│
     │     │                    │                        │
     │     │                    │ 7. json_encode()       │
     │     │  ←─────────────────│ {nombre:"Pepe",        │
     │     │  JSON response     │  saludo:"That's...",   │
     │     │                    │  error:0}              │
     │     │                    │                        │
     │  8. callback(err, res)   │                        │
     │     │                    │                        │
     │     │ 9. Mostrar en DOM  │                        │
     │     │   "Pepe dice:      │                        │
     │     │    That's all..."  │                        │
     │     ▼                    │                        │
```

## 5. Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Aplicacion.html                                          │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ + botonPulsado() : Void     (muestra saludo)            │   │
│  │ + loginPulsado() : Void     (autentica usuario)         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                          │                                      │
│  ┌───────────────────────┴──────────────────────────────────┐   │
│  │ logicaFake/diHola.js                                     │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ + diHola(cb: Function) : Void                            │   │
│  │   Implementación: XMLHttpRequest → GET → rest/diHola.php │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ logicaFake/hacerLogin.js                                 │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ + hacerLogin(nombre, pass, cb) : Void                    │   │
│  │   Implementación: XMLHttpRequest → POST                  │   │
│  │                   → rest/hacerLogin.php                  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                          │
                     HTTP (AJAX)
                          │
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE NEGOCIO                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ rest/hacerLogin.php                                      │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ Variables:                                               │   │
│  │   $objetoResultado : StdClass                             │   │
│  │   $nombre : Texto (de $_POST)                            │   │
│  │   $password : Texto (de $_POST)                          │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ Lógica:                                                  │   │
│  │   1. session_start()                                     │   │
│  │   2. Obtener nombre, password de $_POST                  │   │
│  │   3. Validar inputs                                      │   │
│  │   4. hacerLogin(nombre, password) → boolean              │   │
│  │   5. Si true: $_SESSION["usuario"] = nombre              │   │
│  │   6. Si false: session_destroy()                         │   │
│  │   7. Retornar JSON {resultado, usuario}                  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ rest/diHola.php                                          │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ Variables:                                               │   │
│  │   $objetoResultado : StdClass                             │   │
│  │   $usuario : Texto (de $_SESSION)                        │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ Lógica:                                                  │   │
│  │   1. session_start()                                     │   │
│  │   2. Verificar $_SESSION["usuario"] existe               │   │
│  │   3. Si no existe: retornar {error: "no acreditado"}     │   │
│  │   4. Si existe: diHola(usuario) → {nombre, saludo}       │   │
│  │   5. Retornar JSON {nombre, saludo, error:0}             │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ logica/hacerLogin.php                                    │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ + hacerLogin(nombre:Texto, password:Texto) : Booleano    │   │
│  │   Implementación:                                        │   │
│  │     - password_verify(password, hashAlmacenado)          │   │
│  │     - Retorna true/false                                 │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ logica/diHola.php                                        │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ + diHola(usuario:Texto) : Objeto                         │   │
│  │   Retorna: {nombre: usuario, saludo: "That's all folks"} │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Modelo de Datos - Sesión PHP

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHP SESSION                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Cookie: PHPSESSID = "abc123def456..."                         │
│          (enviada al navegador en Set-Cookie)                   │
│                                                                 │
│  Almacén en servidor:                                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Sesión "abc123def456..."                                │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  $_SESSION["usuario"] = "Pepe"                     │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  Flujo:                                                         │
│  1. session_start() → lee cookie PHPSESSID                      │
│  2. $_SESSION["usuario"] = "Pepe" → guarda en servidor          │
│  3. DiHola → lee $_SESSION["usuario"] → "Pepe"                  │
│  4. session_destroy() → borra datos del servidor                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 7. Tabla de Endpoints

| Método | URL | Parámetros | Retorna | Sesión |
|--------|-----|------------|---------|--------|
| POST | rest/hacerLogin.php | nombre, password (body) | {resultado, usuario} | Crea |
| GET | rest/diHola.php | (ninguno) | {nombre, saludo, error} | Lee |

## 8. Resumen de Tipos de Datos

| Tipo | Dominio | Ejemplo | Uso |
|------|---------|---------|-----|
| Texto | String | "Pepe" | Nombre, password, saludo |
| B | Booleano | true/false | Resultado del login |
| Z | Integer | 0 | Código de error |
| Objeto | StdClass | {nombre, saludo} | Respuesta JSON |
