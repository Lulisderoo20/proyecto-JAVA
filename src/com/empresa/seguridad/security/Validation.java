package com.empresa.seguridad.security;

import java.util.regex.Pattern;

// Reglas simples de validacion para entradas del usuario.
public final class Validation {
    // Username permitido: letras, numeros y algunos simbolos comunes.
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,30}$");
    // Validacion basica de email.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    // Valida nombre de usuario.
    public static boolean validUsername(String value) {
        return value != null && USERNAME.matcher(value).matches();
    }

    // Valida contrasena por largo minimo y maximo.
    public static boolean validPassword(String value) {
        return value != null && value.length() >= 8 && value.length() <= 72;
    }

    // Valida nombre de cliente.
    public static boolean validCustomerName(String value) {
        return value != null && !value.isBlank() && value.length() <= 80;
    }

    // Valida email del cliente.
    public static boolean validEmail(String value) {
        return value != null && value.length() <= 120 && EMAIL.matcher(value).matches();
    }
}
