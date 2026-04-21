package com.empresa.seguridad.http;

import com.empresa.seguridad.security.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// Endpoint de salud.
// Sirve para comprobar rapidamente que la aplicacion esta funcionando.
// `HttpHandler` es una interfaz.
// Una interfaz es un contrato: dice que metodo debe tener una clase.
// `implements` significa que esta clase acepta cumplir ese contrato.
public final class HealthHandler implements HttpHandler {
    // `@Override` otra vez indica que este metodo viene de `HttpHandler`.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Solo acepta GET.
        // `if` significa: "si esta condicion es verdadera, entra en este bloque".
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpSupport.sendJson(exchange, 405, JsonUtil.jsonMessage("error", "Method not allowed"));
            // `return` corta el metodo aqui mismo.
            return;
        }
        // Si todo esta bien, devuelve un JSON minimo.
        HttpSupport.sendJson(exchange, 200, "{\"status\":\"ok\"}");
    }
}
