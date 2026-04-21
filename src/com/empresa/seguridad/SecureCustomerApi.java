// `package` indica en que grupo logico vive esta clase (la o las clases que se definen en este archivo).
// Java usa paquetes para ordenar el codigo y evitar choques de nombres.
package com.empresa.seguridad;

// `import` permite usar clases de otros paquetes sin escribir
// su nombre completo en cada linea.
import com.empresa.seguridad.http.CustomerHandler;
import com.empresa.seguridad.http.HealthHandler;
import com.empresa.seguridad.http.LoginHandler;
import com.empresa.seguridad.security.AuditLogger;
import com.empresa.seguridad.security.Config;
import com.empresa.seguridad.service.CustomerService;
import com.empresa.seguridad.service.UserService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

// Esta es la clase principal del programa.
// Una `class` es un molde: describe que datos y comportamientos puede tener algo.
// A partir de una clase luego puedes crear objetos.
// Cuando ejecutas `java -cp out com.empresa.seguridad.SecureCustomerApi`,
// Java entra por esta clase y por su metodo `main`.
//
// `public` significa que otras partes del programa pueden usar esta clase.
// `final` significa que esta clase no fue pensada para heredarse.
public final class SecureCustomerApi {
    // Un constructor es el bloque que se ejecuta cuando alguien hace `new NombreDeClase(...)`.
    // Constructor privado:
    // esta clase no se quiere usar para crear objetos, solo para ejecutar `main`.
    // `private` significa que solo esta misma clase puede llamar este constructor.
    private SecureCustomerApi() {
    }

    // `main` es el punto de entrada de una aplicacion Java.
    // Todo lo necesario para levantar el servidor se prepara aqui.
    // `static` significa que este metodo pertenece a la clase y no a un objeto concreto.
    // Por eso Java puede ejecutar `main` sin hacer `new SecureCustomerApi()`.
    // `void` significa que este metodo no devuelve un valor.
    // `throws IOException` indica que un posible error de entrada/salida
    // se deja subir en lugar de resolverse aqui mismo.
    //
    // `String[] args` es un arreglo de textos con argumentos de linea de comandos.
    // En este proyecto no se usan, pero Java mantiene esa firma estandar.
    public static void main(String[] args) throws IOException {
        // En `Tipo nombre = valor`, la parte izquierda dice que tipo de dato esperas,
        // `nombre` es la variable y a la derecha queda el valor que guardas.
        // Carga configuraciones desde variables de entorno o valores por defecto.
        Config config = Config.load();
        // `new` crea un objeto en memoria a partir de una clase.
        // Prepara el archivo donde se guardaran eventos importantes de seguridad.
        AuditLogger auditLogger = new AuditLogger(config.auditLogPath());
        // Crea el servicio de usuarios, incluyendo usuarios iniciales.
        UserService userService = new UserService(config);
        // Crea el servicio que maneja los clientes en memoria.
        CustomerService customerService = new CustomerService();
        // Crea el contexto de seguridad: tokens, rate limiting y autenticacion.
        SecurityContext securityContext = new SecurityContext(config, userService, auditLogger);

        // Crea un servidor HTTP simple escuchando en el puerto configurado.
        // `new InetSocketAddress(config.port())` dice en que puerto escuchar.
        HttpServer server = HttpServer.create(new InetSocketAddress(config.port()), 0);
        // Cuando ves `objeto.metodo(...)`, el punto significa:
        // "usa ese objeto para ejecutar una accion".
        // Registra el endpoint /health para saber si la app esta viva.
        server.createContext("/health", new HealthHandler());
        // Registra el endpoint /login para autenticarse.
        server.createContext("/login", new LoginHandler(userService, securityContext, auditLogger));
        // Registra el endpoint /customers para consultar o crear clientes.
        server.createContext("/customers", new CustomerHandler(customerService, securityContext, auditLogger));
        // Define un pool de hilos para atender varias peticiones al mismo tiempo.
        server.setExecutor(Executors.newFixedThreadPool(8));
        // Arranca el servidor.
        server.start();

        // Muestra por consola donde esta escuchando la API.
        System.out.println("SecureCustomerApi listening on http://localhost:" + config.port());
        // Muestra por consola donde se esta guardando el log de auditoria.
        System.out.println("Audit log: " + config.auditLogPath().toAbsolutePath());
    }
}
