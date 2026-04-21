package com.empresa.seguridad.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

// Registra eventos importantes en un archivo.
// Esto sirve para auditoria y seguimiento de seguridad.
public final class AuditLogger {
    private final Path logPath;

    public AuditLogger(Path logPath) throws IOException {
        this.logPath = logPath;
        // Crea la carpeta si aun no existe.
        Files.createDirectories(logPath.getParent());
        // Crea el archivo si es la primera vez.
        if (!Files.exists(logPath)) {
            Files.createFile(logPath);
        }
    }

    // `synchronized` evita que varios hilos escriban mezclado al mismo tiempo.
    public synchronized void log(String event, String actor, String sourceIp) {
        // Arma una linea simple con fecha, evento, actor e IP.
        String line = Instant.now() + " event=" + event + " actor=" + actor + " ip=" + sourceIp + System.lineSeparator();
        try {
            // Agrega la linea al final del archivo.
            Files.writeString(logPath, line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write audit log", e);
        }
    }
}
