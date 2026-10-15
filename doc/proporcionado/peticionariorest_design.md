# Diseño Lógico - PeticionarioREST / PruebaRest1-2022 (Android)

## Diagrama de clases (ASCII)

```
+-------------------------------------------+
|           PeticionarioREST                |
|           (extends AsyncTask)             |
+-------------------------------------------+
| - elMetodo: Texto                         |
| - urlDestino: Texto                       |
| - elCuerpo: Texto                         |
| - laRespuesta: RespuestaREST              |
| - codigoRespuesta: Z                      |
| - cuerpoRespuesta: Texto                  |
+-------------------------------------------+
| + hacerPeticionREST(metodo, url, cuerpo, respuesta): void |
| + doInBackground(): Boolean               |
| + onPostExecute(comoFue: Boolean): void   |
+-------------------------------------------+
          |
          | implementa
          v
+-------------------------------------------+
|       <<interface>> RespuestaREST         |
+-------------------------------------------+
| + callback(codigo: Z, cuerpo: Texto): void |
+-------------------------------------------+

+-------------------------------------------+
|           MainActivity                    |
+-------------------------------------------+
| + onCreate(savedInstanceState): void      |
| + prueba1_pulsado(v: View): void          |
| + probarEnviarPOST(): void                |
| + probarEnviarGET(): void                 |
| + probarEnviarGET_Otra(): void            |
+-------------------------------------------+
          |
          | usa
          v
     PeticionarioREST
```

## Herencia

```
android.os.AsyncTask<Void, Void, Boolean>
         △
         |
PeticionarioREST
```

## Flujo de ejecución (peticion REST)

```
MainActivity
  │
  ├── prueba1_pulsado()
  │     └── probarEnviarPOST()
  │           ├── elPeticionario = new PeticionarioREST()
  │           └── hacerPeticionREST("POST", url, body, callback)
  │                 └── execute() → doInBackground()
  │
  │   PeticionarioREST (WORKER THREAD)
  │     │
  │     └── doInBackground()
  │           ├── URL url = new URL(urlDestino)
  │           ├── connection = url.openConnection()
  │           ├── connection.setRequestMethod(metodo)
  │           ├── connection.setRequestProperty("Content-Type", "application/json")
  │           ├── if (metodo != "GET && cuerpo != null)
  │           │     ├── connection.setDoOutput(true)
  │           │     └── dos.writeBytes(cuerpo)
  │           ├── rc = connection.getResponseCode()
  │           ├── is = connection.getInputStream()
  │           ├── while (linea = br.readLine()) → acumulador.append(linea)
  │           └── return true
  │
  │   onPostExecute(UI THREAD)
  │     └── laRespuesta.callback(codigoRespuesta, cuerpoRespuesta)
```

## Endpoints de prueba

| Metodo | URL                                          | Descripción         |
|--------|----------------------------------------------|----------------------|
| POST   | https://httpbin.org/post                      | Echo POST            |
| GET    | https://jsonplaceholder.typicode.com/users/1234/posts | Posts de usuario |
| GET    | https://reqbin.com/echo                       | Echo GET             |

## Tipos de datos

| Tipo      | Dominio    | Descripción                     |
|-----------|------------|---------------------------------|
| Z         | Entero     | Codigo HTTP de respuesta        |
| Texto     | String     | URL, metodo, cuerpo JSON        |
| Boolean   | Booleano   | Exito de doInBackground()       |

## Aclaraciones del Diseño

- Diseño obtenido por ingeniería inversa del componente proporcionado.
- Se respeta la separación de responsabilidades del código original.

## Reglas Generales

- **Lenguaje objetivo:** el del propio componente proporcionado (Arduino C++ / Java / PHP / JavaScript).
- **Encabezados de funciones:** cada función incluye su diseño lógico en un bloque de comentarios entre líneas discontinuas (`--------------------`).
- **Legibilidad:** código autoexplicativo, con los mínimos comentarios adicionales posibles.
- **Pruebas automatizadas:** se generan casos de prueba para las funciones y métodos clave.
