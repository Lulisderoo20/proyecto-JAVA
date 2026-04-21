package com.empresa.seguridad.security;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Utilidad para hashear y verificar contrasenas usando PBKDF2.
public final class PasswordUtil {
    // Cuantas veces repetir el calculo para volverlo mas costoso.
    private static final int ITERATIONS = 65_536;
    // Largo del resultado en bits.
    private static final int KEY_LENGTH = 256;
    // Tamano del salt en bytes.
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    // Convierte una contrasena en un valor seguro para guardar.
    public static String hashPassword(String password) {
        // El salt es un bloque aleatorio que evita hashes repetidos.
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        // Deriva el hash usando password + salt.
        byte[] hash = derive(password.toCharArray(), salt);
        // Guarda salt y hash juntos en Base64 para poder recuperarlos luego.
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    // Comprueba si una contrasena coincide con el valor almacenado.
    public static boolean verifyPassword(String password, String storedValue) {
        // Se espera el formato salt:hash
        String[] parts = storedValue.split(":");
        if (parts.length != 2) {
            return false;
        }
        // Recupera el salt y el hash esperado.
        byte[] salt = Base64.getDecoder().decode(parts[0].getBytes(StandardCharsets.UTF_8));
        byte[] expected = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
        // Calcula el hash real de la contrasena ingresada.
        byte[] actual = derive(password.toCharArray(), salt);
        // Si el largo no coincide, no puede ser correcto.
        if (actual.length != expected.length) {
            return false;
        }
        // Compara byte por byte sin cortar antes de tiempo.
        // Esto ayuda a evitar ciertos ataques por tiempos.
        int diff = 0;
        for (int i = 0; i < actual.length; i++) {
            diff |= actual[i] ^ expected[i];
        }
        return diff == 0;
    }

    // Metodo interno que hace el trabajo criptografico de PBKDF2.
    private static byte[] derive(char[] password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Si falla algo criptografico, se corta la aplicacion porque es grave.
            throw new IllegalStateException("Unable to hash password", e);
        }
    }
}
