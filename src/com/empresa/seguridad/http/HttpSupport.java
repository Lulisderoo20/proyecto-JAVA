package com.empresa.seguridad.http;

import com.empresa.seguridad.security.SecurityHeaders;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Clase utilitaria para tareas HTTP repetidas.
public final class HttpSupport {
    // Constructor privado porque nadie necesita crear objetos de esta clase.
    private HttpSupport() {
    }

    // Lee todo el cuerpo de la peticion y lo devuelve como texto UTF-8.
    public static String readBody(HttpExchange exchange) throws IOException {
        // `InputStream` es un flujo de bytes que aqui viene desde la request.
        // try-with-resources cierra el stream automaticamente al terminar.
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // Envia una respuesta JSON con codigo de estado y cabeceras de seguridad.
    public static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        // `byte[]` significa "arreglo de bytes".
        // La red trabaja con bytes, por eso el texto se convierte antes de enviarse.
        // Convierte el texto a bytes porque la red transmite bytes, no Strings.
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        // Agrega cabeceras de seguridad a la respuesta.
        SecurityHeaders.apply(exchange.getResponseHeaders());
        // Envia el codigo HTTP y el largo del contenido.
        exchange.sendResponseHeaders(status, bytes.length);
        // Escribe el cuerpo de la respuesta.
        exchange.getResponseBody().write(bytes);
        // Cierra la respuesta y libera recursos.
        exchange.close();
    }
}
