package com.empresa.seguridad.model;

// Representa a un usuario del sistema.
// Guarda nombre, hash de contrasena y rol.
// `record` es una forma compacta de decir:
// "esta clase existe casi solo para guardar datos".
// Java genera automaticamente el constructor y metodos como
// `username()`, `passwordHash()` y `role()`.
public record User(String username, String passwordHash, Role role) {
}
