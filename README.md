# Proyecto Java Seguro

Este proyecto muestra como aplicar controles de ciberseguridad en una aplicacion Java sencilla con enfoque de empresa real.

No incluye interfaz grafica a proposito. La idea es enfocarse primero en el backend sin mezclar ese aprendizaje con una capa visual. Para este tipo de proyecto, la API funciona como la interfaz principal de trabajo y prueba.

Se centra en:

- estructura del proyecto y del codigo
- logica de negocio
- APIs y manejo de requests
- seguridad en aplicaciones Java
- autenticacion y autorizacion
- validacion de entradas
- roles y control de acceso
- configuracion por entorno
- uso de HTTP en un servicio real

Incluye:

- autenticacion con contraseñas hasheadas usando PBKDF2
- autorizacion por roles
- token firmado con HMAC
- rate limiting para login
- validacion de entradas
- headers HTTP de seguridad
- auditoria de eventos
- configuracion por variables de entorno
- endpoint de salud para monitoreo

## Que es PBKDF2

PBKDF2 significa `Password-Based Key Derivation Function 2`.

En este proyecto se usa para proteger contrasenas antes de guardarlas. En lugar de almacenar la contrasena original, la aplicacion:

1. genera un `salt` aleatorio
2. aplica muchas iteraciones del algoritmo
3. guarda el valor derivado junto con el `salt`

Esto vuelve mucho mas caro atacar las claves robadas. Si alguien obtiene la base de usuarios, no ve contrasenas en texto plano y tampoco puede probar millones de combinaciones tan rapido como con un hash simple usado sin protecciones extra.

En corto: PBKDF2 hace el calculo lento a proposito para dificultar ataques de fuerza bruta y tablas precalculadas.

## Modelo del caso

La aplicacion simula un servicio interno de gestion de clientes.

Usuarios iniciales:

- `admin` con rol `ADMIN`
- `analista` con rol `USER`

## Riesgos que este proyecto intenta mitigar

1. Robo de credenciales: las contrasenas no se guardan en texto plano.
2. Escalada de privilegios: cada endpoint controla permisos por rol.
3. Fuerza bruta: el login tiene rate limiting por IP.
4. Manipulacion de sesiones: el token esta firmado y expira.
5. Inyecciones por entrada insegura: las entradas se validan y limitan.
6. Falta de trazabilidad: los eventos importantes se auditan en `logs/audit.log`.
7. Mala gestion de secretos: la clave de firma sale de variables de entorno.

## Variables de entorno

- `APP_PORT` puerto HTTP. Por defecto `8080`.
- `APP_TOKEN_SECRET` secreto para firmar tokens. Si no se define, se usa un valor de demo.
- `APP_ADMIN_PASSWORD` contrasena inicial del usuario `admin`. Por defecto `Admin123!`.
- `APP_ANALYST_PASSWORD` contrasena inicial del usuario `analista`. Por defecto `Analyst123!`.

## Ruta sugerida para leer el codigo si empiezas desde cero

Si no sabes programar todavia, no conviene abrir los archivos al azar. Este proyecto ya tiene comentarios dentro del codigo, pero para aprovecharlos mejor conviene seguir un orden fijo.

Regla de esta guia: cuando un concepto ya fue explicado en un archivo anterior, no hace falta volver a aprenderlo desde cero cada vez que reaparece. La idea es avanzar capa por capa.

1. `src/com/empresa/seguridad/SecureCustomerApi.java`: leelo primero. Es el punto de entrada del programa y muestra el recorrido mas general de la aplicacion. Aqui aparecen por primera vez `package`, `import`, `class`, `public`, `final`, constructor, `private`, `static`, `void`, `main`, variables, `new` y llamadas a metodos.
2. `src/com/empresa/seguridad/http/HealthHandler.java`: leelo segundo porque es el endpoint mas corto y simple. Aqui se entiende que es un handler, que es una interfaz, que significa `implements`, para que sirve `@Override`, que hace un `if` y por que `return` corta la ejecucion del metodo.
3. `src/com/empresa/seguridad/http/HttpSupport.java`: despues mira esta clase de apoyo. Sirve para entender metodos utilitarios, `byte[]`, `InputStream`, `try (...)` y como una respuesta HTTP se arma y se envia.
4. `src/com/empresa/seguridad/model/Role.java`: introduce `enum`, que se usa cuando solo existe un conjunto fijo de opciones.
5. `src/com/empresa/seguridad/model/User.java`: introduce `record`, una forma corta de representar datos.
6. `src/com/empresa/seguridad/model/AuthenticatedUser.java`: muestra otro `record`, pero ya no necesitas reaprender que es porque se explica en `User.java`.
7. `src/com/empresa/seguridad/model/Customer.java`: completa los modelos con otro ejemplo simple de datos.
8. `src/com/empresa/seguridad/service/UserService.java`: aqui empiezan los servicios. Aparecen `Map`, `Optional`, constructores y la idea de guardar datos en memoria.
9. `src/com/empresa/seguridad/service/CustomerService.java`: suma `List`, `AtomicInteger` y otra coleccion pensada para varios hilos.
10. `src/com/empresa/seguridad/security/Config.java`: muestra como la aplicacion lee variables de entorno y usa un `Path` para ubicar archivos.
11. `src/com/empresa/seguridad/security/AuditLogger.java`: introduce escritura en archivos y `synchronized`.
12. `src/com/empresa/seguridad/security/Validation.java`: muestra validaciones simples y expresiones regulares.
13. `src/com/empresa/seguridad/security/JsonUtil.java`: ayuda a ver como se lee y se arma JSON sin usar librerias externas.
14. `src/com/empresa/seguridad/security/PasswordUtil.java`: explica el hash de contrasenas y el uso de `byte[]`, `Base64` y PBKDF2.
15. `src/com/empresa/seguridad/security/TokenService.java`: muestra como se crea y se verifica el token.
16. `src/com/empresa/seguridad/security/RateLimiter.java`: ensena como limitar intentos por tiempo y por IP.
17. `src/com/empresa/seguridad/SecurityContext.java`: junta autenticacion, tokens, rate limiting y usuario autenticado.
18. `src/com/empresa/seguridad/http/LoginHandler.java`: ahora ya puedes leer el login con casi todas las piezas entendidas.
19. `src/com/empresa/seguridad/http/CustomerHandler.java`: dejalo para el final porque combina autenticacion, roles, validacion, servicios y respuestas HTTP en un solo archivo.

Si una persona sigue ese recorrido, primero entiende la forma general del programa y despues entra en detalles. Eso evita que palabras como `class`, `record`, `enum`, `Optional` o `implements` aparezcan de golpe sin contexto.

## Ideas para una segunda version

Si este proyecto se toma como base de aprendizaje, una segunda version podria sumar temas que hoy no estan desarrollados o aparecen solo de forma basica:

- POO mas solida: clases, interfaces, herencia y composicion
- colecciones y manejo de datos en memoria
- excepciones y manejo de errores mas completo
- `streams` y `Optional`
- testing con JUnit
- integracion con base de datos real y SQL
- uso de Spring Boot para acercarlo a un backend Java moderno
- mejores practicas de Git y trabajo por versiones
- ejercicios de logica y algoritmos aplicados al codigo

La idea de esa segunda version seria mantener la base de seguridad de este proyecto y ampliar el nivel tecnico para acercarlo mas a un entorno profesional y a una preparacion mas completa.

## Estructura del proyecto

### Carpetas principales

- `src/`: contiene todo el codigo fuente Java.
- `src/com/empresa/seguridad/`: paquete principal del proyecto. Desde aqui se organiza la aplicacion por responsabilidad.
- `src/com/empresa/seguridad/http/`: contiene los handlers HTTP, es decir, las clases que atienden las peticiones de la API.
- `src/com/empresa/seguridad/model/`: contiene los modelos de datos del sistema, como usuarios, clientes y roles.
- `src/com/empresa/seguridad/security/`: contiene la logica de seguridad y utilidades relacionadas con autenticacion, tokens, validaciones, headers y auditoria.
- `src/com/empresa/seguridad/service/`: contiene servicios con la logica de negocio y acceso a datos en memoria.
- `logs/`: guarda el archivo de auditoria `audit.log`.
- `out/`: carpeta de salida donde se generan los `.class` al compilar.

### Que hace cada archivo

- `src/com/empresa/seguridad/SecureCustomerApi.java`: punto de entrada del programa. Carga configuracion, crea servicios, registra endpoints y levanta el servidor HTTP.
- `src/com/empresa/seguridad/SecurityContext.java`: concentra la seguridad general de la app. Maneja autenticacion por token, rate limiting para login y datos del usuario autenticado.
- `src/com/empresa/seguridad/http/LoginHandler.java`: atiende `POST /login`, valida credenciales y devuelve un token si el usuario inicia sesion correctamente.
- `src/com/empresa/seguridad/http/CustomerHandler.java`: atiende `GET /customers` y `POST /customers`, aplicando autenticacion y control por roles.
- `src/com/empresa/seguridad/http/HealthHandler.java`: atiende `GET /health` para comprobar que la API esta funcionando.
- `src/com/empresa/seguridad/http/HttpSupport.java`: contiene ayuda comun para leer el body de una request y enviar respuestas JSON.
- `src/com/empresa/seguridad/model/User.java`: representa un usuario del sistema.
- `src/com/empresa/seguridad/model/AuthenticatedUser.java`: representa un usuario ya autenticado, con solo los datos necesarios para permisos.
- `src/com/empresa/seguridad/model/Customer.java`: representa un cliente.
- `src/com/empresa/seguridad/model/Role.java`: define los roles posibles del sistema, como `ADMIN` y `USER`.
- `src/com/empresa/seguridad/security/PasswordUtil.java`: hashea y verifica contrasenas usando PBKDF2.
- `src/com/empresa/seguridad/security/TokenService.java`: crea y valida tokens firmados con HMAC.
- `src/com/empresa/seguridad/security/RateLimiter.java`: limita la cantidad de intentos de login por IP en una ventana de tiempo.
- `src/com/empresa/seguridad/security/SecurityHeaders.java`: agrega headers HTTP de seguridad a las respuestas.
- `src/com/empresa/seguridad/security/Validation.java`: valida username, password, email y nombre de cliente.
- `src/com/empresa/seguridad/security/JsonUtil.java`: parsea JSON simple y construye respuestas JSON.
- `src/com/empresa/seguridad/security/Config.java`: carga configuracion desde variables de entorno.
- `src/com/empresa/seguridad/security/AuditLogger.java`: escribe eventos importantes en el log de auditoria.
- `src/com/empresa/seguridad/service/UserService.java`: administra usuarios en memoria y verifica credenciales.
- `src/com/empresa/seguridad/service/CustomerService.java`: administra clientes en memoria, permitiendo listar y crear.
- `.gitignore`: evita subir al repositorio archivos que no conviene versionar, como compilados o logs.
- `README.md`: documentacion principal del proyecto.

### Por que no se puede leer facilmente `SecureCustomerApi.class`

El archivo `SecureCustomerApi.class` no es codigo fuente. Es el resultado de compilar `SecureCustomerApi.java`.

Eso significa que:

- `SecureCustomerApi.java` es el archivo que una persona puede leer y editar
- `SecureCustomerApi.class` es un archivo binario que la JVM ejecuta

Por eso, cuando abres un `.class`, no ves el contenido como texto normal. No esta pensado para aprendizaje directo ni para modificarse a mano, porque ya no guarda el codigo con formato humano original, sino bytecode compilado.

En este proyecto, si quieres entender como funciona la aplicacion, debes leer los archivos dentro de `src/`, no los de `out/`.

La carpeta `out/` existe solo como salida de compilacion:

- `src/` = codigo fuente legible
- `out/` = codigo compilado para ejecutar

Si alguna vez quieres inspeccionar un `.class`, normalmente se usa un descompilador, pero para aprender Java conviene ir siempre al `.java` original.

### Que es la JVM

JVM significa `Java Virtual Machine`.

Es el componente que se encarga de ejecutar programas Java compilados. En otras palabras:

- tu escribes codigo en archivos `.java`
- el compilador `javac` transforma ese codigo en archivos `.class`
- la JVM toma esos `.class` y los ejecuta

Por eso en Java normalmente hay dos momentos distintos:

1. compilacion: convertir codigo fuente en bytecode
2. ejecucion: hacer que la JVM corra ese bytecode

### Que es el bytecode

El bytecode es una forma intermedia entre el codigo que escribes y el codigo maquina real del sistema operativo.

No esta pensado para que una persona lo lea facilmente. Esta pensado para que la JVM lo entienda y lo ejecute.

La idea es que el mismo programa Java compilado pueda ejecutarse en distintos sistemas que tengan una JVM compatible, por ejemplo Windows, Linux o macOS.

En este proyecto, el flujo es asi:

1. escribes y lees el codigo en `src/.../*.java`
2. compilas con `javac`
3. eso genera archivos `.class` dentro de `out/`
4. luego ejecutas `java -cp out com.empresa.seguridad.SecureCustomerApi`
5. la JVM busca esa clase compilada y empieza a correr la aplicacion

Dicho simple:

- `javac` compila
- la JVM ejecuta
- los `.java` los lees tu
- los `.class` los entiende la JVM

## Arquitectura y estilo de programacion

Este proyecto no sigue una arquitectura empresarial grande como microservicios, hexagonal o clean architecture completa. En cambio, usa una arquitectura simple por capas y por responsabilidades, que es muy util para aprender.

La separacion principal es esta:

- capa HTTP: recibe requests y devuelve responses
- capa de servicios: aplica la logica de negocio
- capa de seguridad: concentra autenticacion, tokens, validacion y controles de seguridad
- capa de modelos: representa los datos con los que trabaja la aplicacion

En la practica, el flujo general es asi:

1. llega una request a un handler HTTP
2. el handler valida el metodo, los datos y la autenticacion si hace falta
3. el handler llama a un servicio
4. el servicio trabaja con los datos en memoria
5. se devuelve una respuesta JSON

### Que paradigma usa

El proyecto esta orientado principalmente a programacion orientada a objetos, porque trabaja con:

- clases
- objetos
- servicios
- modelos
- encapsulamiento de responsabilidades

Tambien tiene algo de logica estructurada o procedural en algunos metodos, porque varias operaciones se resuelven paso a paso dentro de un mismo flujo, por ejemplo validar, autenticar, registrar auditoria y responder.

No esta orientado a programacion funcional como enfoque principal. Hay algunas ideas modernas de Java, como `Optional`, `record` y colecciones concurrentes, pero el diseno general sigue siendo orientado a objetos con una estructura por capas.

En resumen:

- paradigma principal: orientado a objetos
- estilo complementario: estructurado/procedural en el flujo de los metodos
- enfoque arquitectonico: backend monolitico pequeno, organizado por capas y responsabilidades

## Compilar

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

## Ejecutar

```powershell
java -cp out com.empresa.seguridad.SecureCustomerApi
```

## Pruebas manuales

### 1. Salud

```powershell
Invoke-RestMethod http://localhost:8080/health
```

### 2. Login

```powershell
$body = @{ username = 'admin'; password = 'Admin123!' } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/login -Body $body -ContentType 'application/json'
$token = $login.token
```

### 3. Consultar clientes

```powershell
Invoke-RestMethod -Headers @{ Authorization = "Bearer $token" } http://localhost:8080/customers
```

### 4. Crear cliente

```powershell
$newCustomer = @{ name = 'Acme Corp'; email = 'security@acme.com' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Headers @{ Authorization = "Bearer $token" } -Uri http://localhost:8080/customers -Body $newCustomer -ContentType 'application/json'
```

## Como se aplica esto en una empresa real

Este proyecto no reemplaza herramientas empresariales, pero refleja practicas reales:

- seguridad desde el diseno
- autenticacion y autorizacion separadas
- configuracion segura por entorno
- observabilidad y auditoria
- controles preventivos y detectivos
- principio de minimo privilegio

El siguiente paso profesional seria conectarlo con:

- base de datos real
- gestor de secretos
- TLS reverso
- SIEM
- CI/CD con SAST y escaneo de dependencias
- pruebas de seguridad automatizadas
