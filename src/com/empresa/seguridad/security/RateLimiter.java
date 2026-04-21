package com.empresa.seguridad.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Limita cuantas veces se puede intentar una accion en cierto tiempo.
// En este proyecto se usa para el login.
public final class RateLimiter {
    private final int maxAttempts;
    private final long windowMillis;
    // Guarda el estado de intentos por clave.
    // En login, la clave es la IP del cliente.
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public RateLimiter(int maxAttempts, long windowMillis) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
    }

    // Devuelve true si la clave todavia puede intentar.
    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        // Si la clave no existia, crea una nueva ventana de intentos.
        AttemptWindow window = attempts.computeIfAbsent(key, ignored -> new AttemptWindow(now, 0));
        // Sincroniza sobre esa ventana para evitar carreras entre hilos.
        synchronized (window) {
            // Si la ventana actual ya vencio, reinicia contador.
            if (now - window.windowStart > windowMillis) {
                window.windowStart = now;
                window.count = 0;
            }
            // Cuenta este intento.
            window.count++;
            // Solo permite hasta el maximo configurado.
            return window.count <= maxAttempts;
        }
    }

    // Clase interna que representa una ventana de tiempo para una clave.
    private static final class AttemptWindow {
        private long windowStart;
        private int count;

        private AttemptWindow(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
