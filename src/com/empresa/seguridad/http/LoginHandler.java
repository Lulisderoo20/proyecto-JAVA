// Paquete donde vive esta clase.
package com.empresa.seguridad.http;

import com.empresa.seguridad.SecurityContext;
import com.empresa.seguridad.model.User;
import com.empresa.seguridad.security.AuditLogger;
import com.empresa.seguridad.security.JsonUtil;
import com.empresa.seguridad.security.Validation;
import com.empresa.seguridad.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// Un handler es la clase que atiende un endpoint HTTP.
// Esta clase maneja las peticiones a /login.
// Igual que en `CustomerHandler`, `implements HttpHandler` obliga a definir `handle(...)`.
public final class LoginHandler implements HttpHandler {
    private final UserService userService;
    private final SecurityContext securityContext;
    private final AuditLogger auditLogger;

    // Constructor: recibe las dependencias que esta clase usara.
    public LoginHandler(UserService userService, SecurityContext securityContext, AuditLogger auditLogger) {
        this.userService = userService;
        this.securityContext = securityContext;
        this.auditLogger = auditLogger;
    }

    // `@Override` indica que este metodo implementa el metodo de la interfaz `HttpHandler`.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Solo se permite POST para login.
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpSupport.sendJson(exchange, 405, JsonUtil.jsonMessage("error", "Method not allowed"));
            return;
        }

        // Obtiene la IP del cliente.
        String clientIp = SecurityContext.clientIp(exchange);
        // Revisa si la IP no excedio el limite de intentos.
        if (!securityContext.allowLoginAttempt(clientIp)) {
            auditLogger.log("auth.rate_limited", "anonymous", clientIp);
            HttpSupport.sendJson(exchange, 429, JsonUtil.jsonMessage("error", "Too many login attempts"));
            return;
        }

        // Lee el cuerpo de la peticion, que deberia traer JSON.
        String body = HttpSupport.readBody(exchange);
        // Convierte un JSON simple en un mapa de texto.
        Map<String, String> payload = JsonUtil.parseFlatObject(body);
        // `get("username")` busca el valor asociado a esa clave.
        // Extrae username y password del JSON recibido.
        String username = payload.get("username");
        String password = payload.get("password");

        // Valida que el formato minimo de los datos sea aceptable.
        if (!Validation.validUsername(username) || !Validation.validPassword(password)) {
            auditLogger.log("auth.invalid_input", username == null ? "anonymous" : username, clientIp);
            HttpSupport.sendJson(exchange, 400, JsonUtil.jsonMessage("error", "Invalid credentials format"));
            return;
        }

        // Intenta autenticar con el servicio de usuarios.
        Optional<User> user = userService.authenticate(username, password);
        // Si el usuario no existe o la contrasena es incorrecta, el Optional viene vacio.
        if (user.isEmpty()) {
            auditLogger.log("auth.failed", username, clientIp);
            HttpSupport.sendJson(exchange, 401, JsonUtil.jsonMessage("error", "Invalid credentials"));
            return;
        }

        // Si el login fue correcto, crea un token para futuras peticiones.
        // `user.get()` obtiene el valor guardado dentro del Optional.
        // Solo se debe usar cuando ya comprobaste que no esta vacio.
        String token = securityContext.issueToken(user.get());
        auditLogger.log("auth.success", username, clientIp);
        // Devuelve el token al cliente.
        HttpSupport.sendJson(exchange, 200, JsonUtil.jsonToken(token));
    }
}
