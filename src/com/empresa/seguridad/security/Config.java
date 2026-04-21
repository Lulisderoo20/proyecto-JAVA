package com.empresa.seguridad.security;

import java.nio.file.Path;

// Un `record` es una forma compacta de representar datos.
// Esta clase guarda la configuracion de la app.
// `Path` representa la ubicacion de un archivo o carpeta.
public record Config(int port, String tokenSecret, String adminPassword, String analystPassword, Path auditLogPath) {
    // Carga valores desde variables de entorno.
    // Si una variable no existe, usa un valor por defecto.
    public static Config load() {
        // `System.getenv()` permite leer variables de entorno del sistema operativo.
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        String tokenSecret = System.getenv().getOrDefault("APP_TOKEN_SECRET", "demo-secret-change-in-production");
        String adminPassword = System.getenv().getOrDefault("APP_ADMIN_PASSWORD", "Admin123!");
        String analystPassword = System.getenv().getOrDefault("APP_ANALYST_PASSWORD", "Analyst123!");
        // Define la ruta del archivo de auditoria.
        Path auditLogPath = Path.of("logs", "audit.log");
        return new Config(port, tokenSecret, adminPassword, analystPassword, auditLogPath);
    }
}
