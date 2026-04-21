package com.empresa.seguridad;

import com.empresa.seguridad.model.AuthenticatedUser;
import com.empresa.seguridad.model.User;
import com.empresa.seguridad.security.AuditLogger;
import com.empresa.seguridad.security.Config;
import com.empresa.seguridad.security.RateLimiter;
import com.empresa.seguridad.security.TokenService;
import com.empresa.seguridad.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import java.util.List;
import java.util.Optional;

// Esta clase agrupa piezas relacionadas con seguridad.
// Su trabajo es centralizar login, tokens, rate limiting y autenticacion.
public final class SecurityContext {
    // Servicio encargado de crear y validar tokens.
    private final TokenService tokenService;
    // Servicio donde se buscan usuarios del sistema.
    private final UserService userService;
    // Limita cuantos intentos de login se permiten por ventana de tiempo.
    private final RateLimiter loginRateLimiter;
    // Permite registrar eventos de seguridad.
    private final AuditLogger auditLogger;

    public SecurityContext(Config config, UserService userService, AuditLogger auditLogger) {
        // Usa el secreto configurado para firmar tokens.
        this.tokenService = new TokenService(config.tokenSecret());
        this.userService = userService;
        // Permite hasta 5 intentos en 60 segundos por IP.
        this.loginRateLimiter = new RateLimiter(5, 60_000L);
        this.auditLogger = auditLogger;
    }

    // Pregunta si una IP todavia puede intentar iniciar sesion.
    public boolean allowLoginAttempt(String clientIp) {
        return loginRateLimiter.allow(clientIp);
    }

    // Genera un token para un usuario autenticado.
    public String issueToken(User user) {
        return tokenService.issueToken(user.username(), user.role().name());
    }

    // Revisa la cabecera Authorization y valida el token recibido.
    public Optional<AuthenticatedUser> authenticate(HttpExchange exchange) {
        // Busca el header Authorization dentro de la peticion HTTP.
        // `List<String>` significa "lista de textos".
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        // Si no existe, no hay usuario autenticado.
        if (authHeaders == null || authHeaders.isEmpty()) {
            return Optional.empty();
        }
        // Toma el primer valor del header.
        String authValue = authHeaders.get(0);
        // Espera el formato: Bearer <token>
        if (!authValue.startsWith("Bearer ")) {
            return Optional.empty();
        }
        // Extrae solo el token, quitando la palabra "Bearer".
        String token = authValue.substring("Bearer ".length()).trim();
        // Verifica firma, contenido y expiracion del token.
        Optional<TokenService.TokenClaims> claims = tokenService.verify(token);
        if (claims.isEmpty()) {
            return Optional.empty();
        }

        // Busca al usuario real en memoria usando el username guardado en el token.
        // `claims.get().username()` llama al metodo generado automaticamente por el `record`.
        Optional<User> user = userService.findByUsername(claims.get().username());
        if (user.isEmpty()) {
            // Si el token habla de un usuario inexistente, se registra el evento.
            auditLogger.log("auth.user_not_found", claims.get().username(), clientIp(exchange));
            return Optional.empty();
        }
        // Devuelve una version reducida del usuario ya autenticado.
        return Optional.of(new AuthenticatedUser(user.get().username(), user.get().role()));
    }

    // Obtiene la IP del cliente desde la conexion HTTP actual.
    public static String clientIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
