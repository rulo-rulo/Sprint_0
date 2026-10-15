# Ingeniería Inversa - PeticionarioREST / PruebaRest1-2022

## 1. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CLIENTE REST ANDROID                                  │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  CAPA DE PRESENTACIÓN                                            │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  MainActivity                                              │  │   │
│  │  │  - Botón "Prueba 1" → prueba1_pulsado()                    │  │   │
│  │  │  - Botón "Prueba 2" → probarEnviarGET()                    │  │   │
│  │  │  - Botón "Prueba 3" → probarEnviarGET_Otra()               │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    │ llama                              │
│                                    ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  CAPA DE COMUNICACIÓN                                            │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  PeticionarioREST (extends AsyncTask)                      │  │   │
│  │  │                                                            │  │   │
│  │  │  Interface RespuestaREST:                                  │  │   │
│  │  │    + callback(codigo:Z, cuerpo:Texto) : Void               │  │   │
│  │  │                                                            │  │   │
│  │  │  Métodos:                                                  │  │   │
│  │  │    + hacerPeticionREST(met, url, body, cb) : Void          │  │   │
│  │  │    + doInBackground() : Booleano                           │  │   │
│  │  │    + onPostExecute(comoFue) : Void                         │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                              HTTP (Android)                             │
│                                    │                                    │
│                                    ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  SERVIDOR EXTERNO                                                │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  httpbin.org/post   → Echo del POST recibido              │  │   │
│  │  │  jsonplaceholder    → Datos de prueba (posts, usuarios)   │  │   │
│  │  │  reqbin.com         → Echo de peticiones                  │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2. Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MainActivity                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Métodos:                                                           │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + onCreate(savedInstanceState: Bundle) : Void                 │   │
│  │   └→ setContentView(R.layout.activity_main)                  │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ + prueba1_pulsado(v: View) : Void                            │   │
│  │   └→ probarEnviarPOST()                                      │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - probarEnviarPOST() : Void                                  │   │
│  │   └→ PeticionarioREST.hacerPeticionREST(                     │   │
│  │        "POST",                                               │   │
│  │        "https://httpbin.org/post",                            │   │
│  │        "{ 'title': '...', 'body': '...', 'userId': 1234}",   │   │
│  │        callback)                                             │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - probarEnviarGET() : Void                                   │   │
│  │   └→ PeticionarioREST.hacerPeticionREST(                     │   │
│  │        "GET",                                                │   │
│  │        "https://jsonplaceholder.typicode.com/users/1234/posts",│  │
│  │        null, callback)                                       │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ - probarEnviarGET_Otra() : Void                              │   │
│  │   └→ PeticionarioREST.hacerPeticionREST(                     │   │
│  │        "GET",                                                │   │
│  │        "https://reqbin.com/echo",                             │   │
│  │        null, callback)                                       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ crea instancia
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PeticionarioREST                                  │
│                    (extends AsyncTask<Void, Void, Boolean>)          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Atributos:                                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ - elMetodo       : Texto        ("GET", "POST", etc.)       │   │
│  │ - urlDestino     : Texto        (URL completa)              │   │
│  │ - elCuerpo       : Texto        (JSON body, null para GET)  │   │
│  │ - laRespuesta    : RespuestaREST (callback)                 │   │
│  │ - codigoRespuesta: Entero        (HTTP status code)         │   │
│  │ - cuerpoRespuesta: Texto        (response body)             │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Interface:                                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ <<interface>> RespuestaREST                                 │   │
│  │   + callback(codigo:Z, cuerpo:Texto) : Void                 │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Métodos:                                                           │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ + hacerPeticionREST(met, url, body, cb) : Void               │   │
│  │   └→ Almacena params → execute() → lanza hilo               │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ + doInBackground() : Booleano  [WORKER THREAD]               │   │
│  │   └→ HTTP request completa                                   │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │ + onPostExecute(comoFue) : Void  [UI THREAD]                │   │
│  │   └→ laRespuesta.callback(codigo, cuerpo)                   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 3. Diagrama de Secuencia - Petición POST

```
  UI THREAD              WORKER THREAD           SERVIDOR HTTP
     │                        │                       │
     │ 1. prueba1_pulsado()   │                       │
     │    │                   │                       │
     │ 2. new PeticionarioREST│                       │
     │ 3. hacerPeticionREST(  │                       │
     │    "POST", url, body,  │                       │
     │    callback)           │                       │
     │    │                   │                       │
     │ 4. execute()           │                       │
     │  ────────────────────→ │                       │
     │    (lanza hilo)        │                       │
     │                        │                       │
     │  [UI THREAD libre]     │ 5. doInBackground()   │
     │                        │    │                  │
     │                        │ 6. new URL(url)       │
     │                        │    │                  │
     │                        │ 7. url.openConnection()│
     │                        │    │                  │
     │                        │ 8. setRequestMethod   │
     │                        │    ("POST")           │
     │                        │    │                  │
     │                        │ 9. setRequestProperty │
     │                        │    Content-Type:      │
     │                        │    application/json   │
     │                        │    │                  │
     │                        │ 10. setDoOutput(true) │
     │                        │     │                 │
     │                        │ 11. DataOutputStream  │
     │                        │     .writeBytes(body) │
     │                        │     │                 │
     │                        │ 12. flush() + close() │
     │                        │     │                 │
     │                        │ 13. ══════════════════│═══════════→
     │                        │     │  HTTP POST      │
     │                        │     │  Body: {JSON}   │
     │                        │     │                 │
     │                        │ 14. getResponseCode() │
     │                        │     │  ←──────────────│═══════════
     │                        │     │  200 OK         │
     │                        │     │                 │
     │                        │ 15. getInputStream()  │
     │                        │     │                 │
     │                        │ 16. BufferedReader    │
     │                        │     .readLine()       │
     │                        │     │  ←──────────────│═══════════
     │                        │     │  {JSON response}│
     │                        │     │                 │
     │                        │ 17. cuerpoRespuesta = │
     │                        │     acumulador.toString│
     │                        │     │                 │
     │                        │ 18. disconnect()      │
     │                        │     │                 │
     │                        │ 19. return true       │
     │  ←─────────────────────│                       │
     │                        │                       │
     │ 20. onPostExecute(true)│                       │
     │    │                   │                       │
     │ 21. laRespuesta        │                       │
     │     .callback(200,     │                       │
     │     "{...response...}")│                       │
     │    │                   │                       │
     │ 22. Log.d("codigo=200")│                       │
     │     Log.d("cuerpo=...")│                       │
     │                        │                       │
```

## 4. Flujo de Datos - POST

```
┌──────────────────────────────────────────────────────────────┐
│  DATOS DE ENTRADA                                            │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  metodo = "POST"                                     │    │
│  │  url = "https://httpbin.org/post"                    │    │
│  │  body = "{ 'title': 'El Conde de Montecristo',      │    │
│  │           'body': 'Puros Habanos',                   │    │
│  │           'userId': 1234 }"                          │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  PETICION HTTP                                       │    │
│  │                                                      │    │
│  │  POST /post HTTP/1.1                                 │    │
│  │  Host: httpbin.org                                   │    │
│  │  Content-Type: application/json; charset=utf-8       │    │
│  │  Content-Length: 72                                  │    │
│  │                                                      │    │
│  │  {"title":"El Conde de Montecristo",                 │    │
│  │   "body":"Puros Habanos",                            │    │
│  │   "userId":1234}                                     │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  RESPUESTA HTTP                                      │    │
│  │                                                      │    │
│  │  HTTP/1.1 200 OK                                     │    │
│  │  Content-Type: application/json                      │    │
│  │                                                      │    │
│  │  {                                                   │    │
│  │    "args": {},                                       │    │
│  │    "data": "{\"title\":\"El Conde de Montecristo\",  │    │
│  │             \"body\":\"Puros Habanos\",              │    │
│  │             \"userId\":1234}",                       │    │
│  │    "headers": {...},                                 │    │
│  │    "url": "http://httpbin.org/post"                  │    │
│  │  }                                                   │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  DATOS DE SALIDA (callback)                          │    │
│  │  codigo = 200                                        │    │
│  │  cuerpo = "{...JSON response...}"                    │    │
│  └──────────────────────────────────────────────────────┘    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## 5. Flujo de Datos - GET

```
┌──────────────────────────────────────────────────────────────┐
│  DATOS DE ENTRADA                                            │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  metodo = "GET"                                      │    │
│  │  url = "https://jsonplaceholder.typicode.com/        │    │
│  │         users/1234/posts"                            │    │
│  │  body = null                                         │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  PETICION HTTP                                       │    │
│  │                                                      │    │
│  │  GET /users/1234/posts HTTP/1.1                      │    │
│  │  Host: jsonplaceholder.typicode.com                  │    │
│  │  Content-Type: application/json; charset=utf-8       │    │
│  │  (sin body)                                          │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  RESPUESTA HTTP                                      │    │
│  │                                                      │    │
│  │  HTTP/1.1 200 OK                                     │    │
│  │  [...]  (array de posts, vacío si usuario no existe) │    │
│  └──────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  DATOS DE SALIDA (callback)                          │    │
│  │  codigo = 200                                        │    │
│  │  cuerpo = "[...]"                                    │    │
│  └──────────────────────────────────────────────────────┘    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## 6. Diagrama de Estados - AsyncTask

```
              ┌─────────────┐
              │  CREADO     │
              │ (new)       │
              └──────┬──────┘
                     │ execute()
                     ▼
              ┌─────────────┐
              │  EJECUTANDO │
              │ (doIn       │
              │  Background)│
              └──────┬──────┘
                     │ return true/false
                     ▼
              ┌─────────────┐
              │ COMPLETADO  │
              │ (onPost    │
              │  Execute)   │
              └─────────────┘
```

## 7. Resumen de Tipos de Datos

| Tipo | Dominio | Ejemplo | Uso |
|------|---------|---------|-----|
| Texto | String | "POST", "https://..." | Método HTTP, URL, body JSON |
| Z | Integer | 200, 404 | Código HTTP de respuesta |
| B | Booleano | true/false | Éxito de doInBackground() |
| RespuestaREST | Interface | callback() | Callback asíncrono de respuesta |

## 8. Tabla de Endpoints de Prueba

| Método | URL | Body | Descripción |
|--------|-----|------|-------------|
| POST | https://httpbin.org/post | JSON (title, body, userId) | Echo POST |
| GET | https://jsonplaceholder.typicode.com/users/1234/posts | null | Posts de usuario |
| GET | https://reqbin.com/echo | null | Echo GET |
