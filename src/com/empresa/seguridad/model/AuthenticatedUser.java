package com.empresa.seguridad.model;

// Version reducida del usuario ya autenticado.
// Solo expone lo necesario para permisos y auditoria.
public record AuthenticatedUser(String username, Role role) {
}
