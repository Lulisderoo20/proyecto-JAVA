// `package` indica en que carpeta logica vive esta clase dentro del proyecto.
// Java usa paquetes para organizar el codigo y evitar conflictos de nombres.
package com.empresa.seguridad.http;

// `import` permite usar clases de otros paquetes sin escribir su nombre completo cada vez.
import com.empresa.seguridad.SecurityContext;
import com.empresa.seguridad.model.AuthenticatedUser;
import com.empresa.seguridad.model.Customer;
import com.empresa.seguridad.model.Role;
import com.empresa.seguridad.security.AuditLogger;
import com.empresa.seguridad.security.JsonUtil;
import com.empresa.seguridad.security.Validation;
import com.empresa.seguridad.service.CustomerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// Este handler atiende el endpoint /customers.
// Permite listar clientes y crear nuevos, segun el metodo HTTP y el rol.
//
// `public` significa que esta clase puede usarse desde otros paquetes.
// `final` significa que esta clase no esta pensada para ser heredada.
// `implements HttpHandler` significa que esta clase promete cumplir el contrato
// de `HttpHandler`, o sea, tener un metodo `handle(...)` para procesar requests.
public final class CustomerHandler implements HttpHandler {
    // `private final`:
    // - `private` = solo esta clase puede usar este atributo directamente
    // - `final` = una vez asignado en el constructor, no se cambia
    private final CustomerService customerService;
    private final SecurityContext securityContext;
    private final AuditLogger auditLogger;

    // El constructor se ejecuta cuando alguien crea un objeto `CustomerHandler`.
    // Aqui se reciben dependencias que esta clase necesita para trabajar.
    public CustomerHandler(CustomerService customerService, SecurityContext securityContext, AuditLogger auditLogger) {
        this.customerService = customerService;
        this.securityContext = securityContext;
        this.auditLogger = auditLogger;
    }

    // `@Override` es una anotacion.
    // Le dice a Java: "este metodo esta sobrescribiendo uno que ya existe
    // en una interfaz o clase padre".
    //
    // Aqui se usa porque `HttpHandler` exige un metodo llamado `handle`.
    // Si escribieramos mal el nombre o la firma del metodo, Java nos avisaria.
    @Override
    // `public` = el servidor HTTP puede llamar este metodo.
    // `void` = este metodo no devuelve un valor directo.
    // `throws IOException` = este metodo podria fallar por temas de entrada/salida,
    // por ejemplo leyendo la request o escribiendo la response.
    public void handle(HttpExchange exchange) throws IOException {
        // Antes de hacer nada, exige un usuario autenticado.
        // `Optional<AuthenticatedUser>` significa:
        // "puede haber un usuario autenticado o puede no haberlo".
        Optional<AuthenticatedUser> user = securityContext.authenticate(exchange);
        // `isEmpty()` pregunta si el Optional vino vacio.
        if (user.isEmpty()) {
            HttpSupport.sendJson(exchange, 401, JsonUtil.jsonMessage("error", "Unauthorized"));
            return;
        }

        // Guarda datos utiles de la peticion actual.
        String clientIp = SecurityContext.clientIp(exchange);
        // `toUpperCase()` convierte el texto a mayusculas para comparar mas facil.
        String method = exchange.getRequestMethod().toUpperCase();

        // GET /customers devuelve la lista.
        // `"GET".equals(method)` compara Strings de forma segura.
        if ("GET".equals(method)) {
            auditLogger.log("customer.list", user.get().username(), clientIp);
            HttpSupport.sendJson(exchange, 200, JsonUtil.customerListJson(customerService.list()));
            return;
        }

        // POST /customers crea un cliente nuevo.
        if ("POST".equals(method)) {
            // Solo ADMIN puede crear clientes.
            // `!=` significa "distinto de".
            if (user.get().role() != Role.ADMIN) {
                auditLogger.log("customer.create.denied", user.get().username(), clientIp);
                HttpSupport.sendJson(exchange, 403, JsonUtil.jsonMessage("error", "Forbidden"));
                return;
            }

            // Lee y parsea el JSON enviado por el cliente.
            String body = HttpSupport.readBody(exchange);
            // `Map<String, String>` significa:
            // una estructura clave -> valor donde ambas cosas son texto.
            Map<String, String> payload = JsonUtil.parseFlatObject(body);
            String name = payload.get("name");
            String email = payload.get("email");

            // Revisa que nombre e email tengan formato valido.
            if (!Validation.validCustomerName(name) || !Validation.validEmail(email)) {
                HttpSupport.sendJson(exchange, 400, JsonUtil.jsonMessage("error", "Invalid customer data"));
                return;
            }

            // Limpia un poco los datos y crea el cliente.
            // `trim()` quita espacios sobrantes al principio y al final.
            // `toLowerCase()` pasa el email a minusculas.
            Customer customer = customerService.create(name.trim(), email.trim().toLowerCase());
            auditLogger.log("customer.create", user.get().username(), clientIp);
            // Responde con codigo 201, que significa "creado".
            HttpSupport.sendJson(exchange, 201, JsonUtil.customerJson(customer));
            return;
        }

        // Cualquier otro metodo queda rechazado.
        HttpSupport.sendJson(exchange, 405, JsonUtil.jsonMessage("error", "Method not allowed"));
    }
}
