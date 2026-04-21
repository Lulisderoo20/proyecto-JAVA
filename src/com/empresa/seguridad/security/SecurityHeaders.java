package com.empresa.seguridad.security;

import com.sun.net.httpserver.Headers;

// Agrega cabeceras HTTP utiles para endurecer respuestas.
public final class SecurityHeaders {
    private SecurityHeaders() {
    }

    public static void apply(Headers headers) {
        // Informa que la respuesta es JSON en UTF-8.
        headers.set("Content-Type", "application/json; charset=utf-8");
        // Evita que el navegador "adivine" otro tipo de contenido.
        headers.set("X-Content-Type-Options", "nosniff");
        // Evita que la pagina se embeba dentro de frames.
        headers.set("X-Frame-Options", "DENY");
        // No enviar la URL anterior como referencia.
        headers.set("Referrer-Policy", "no-referrer");
        // Indica no guardar la respuesta en cache.
        headers.set("Cache-Control", "no-store");
        // Politica muy cerrada: no cargar recursos externos.
        headers.set("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");
    }
}
