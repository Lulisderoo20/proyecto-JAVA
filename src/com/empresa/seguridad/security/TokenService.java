package com.empresa.seguridad.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Crea y valida tokens firmados con HMAC SHA-256.
// El formato se parece a JWT, aunque esta implementado de forma manual.
public final class TokenService {
    // El token dura 15 minutos.
    private static final long EXPIRATION_SECONDS = 900L;
    // Secreto compartido usado para firmar.
    private final byte[] secret;

    public TokenService(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    // Genera un token para un usuario y un rol.
    public String issueToken(String username, String role) {
        // Fecha de expiracion, en segundos Unix.
        long exp = Instant.now().getEpochSecond() + EXPIRATION_SECONDS;
        // Parte 1: cabecera del token.
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        // Parte 2: datos del usuario.
        String payload = base64Url("{\"sub\":\"" + escape(username) + "\",\"role\":\"" + escape(role) + "\",\"exp\":" + exp + "}");
        // Parte 3: firma de header + payload.
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    // Verifica si el token tiene formato valido, firma valida y no esta vencido.
    public Optional<TokenClaims> verify(String token) {
        // `split("\\.")` separa el texto usando el punto como divisor.
        // Se escribe `\\.` porque el punto tiene significado especial en expresiones regulares.
        String[] parts = token.split("\\.");
        // Un token valido debe tener exactamente 3 partes.
        if (parts.length != 3) {
            return Optional.empty();
        }
        // Recalcula la firma esperada.
        String expected = sign(parts[0] + "." + parts[1]);
        // Compara la firma esperada contra la recibida.
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        // Decodifica el payload para leer sus datos.
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String username = JsonUtil.readString(payload, "sub");
        String role = JsonUtil.readString(payload, "role");
        long exp = JsonUtil.readLong(payload, "exp");
        // Si falta algo o ya expiro, el token no sirve.
        if (username == null || role == null || exp < Instant.now().getEpochSecond()) {
            return Optional.empty();
        }
        // Devuelve los datos leidos del token.
        return Optional.of(new TokenClaims(username, role, exp));
    }

    // Firma un texto con HMAC SHA-256.
    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }

    // Convierte texto normal a Base64 URL-safe, sin relleno.
    private static String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    // Escapa caracteres especiales dentro del JSON.
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // `record` para devolver datos del token ya verificado.
    // Un `record` es una forma corta de crear una clase de solo datos.
    // Java genera automaticamente constructor, getters especiales, equals, hashCode y toString.
    public record TokenClaims(String username, String role, long expiresAt) {
    }
}
