package com.empresa.seguridad.service;

import com.empresa.seguridad.model.Role;
import com.empresa.seguridad.model.User;
import com.empresa.seguridad.security.Config;
import com.empresa.seguridad.security.PasswordUtil;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Este servicio guarda y consulta usuarios.
// En este proyecto se usa memoria, no una base de datos real.
public final class UserService {
    // Lo que va entre `< >` son tipos genericos.
    // Aqui significa: clave de tipo `String` y valor de tipo `User`.
    // Mapa clave-valor:
    // la clave es el username y el valor es el objeto User.
    // `ConcurrentHashMap` se usa porque es mas seguro cuando varios hilos acceden a la vez.
    private final Map<String, User> users = new ConcurrentHashMap<>();

    // Como se llama igual que la clase y no tiene tipo de retorno,
    // esto es un constructor.
    public UserService(Config config) {
        // Crea el usuario admin con su contrasena hasheada.
        users.put("admin", new User("admin", PasswordUtil.hashPassword(config.adminPassword()), Role.ADMIN));
        // Crea el usuario analista con rol mas limitado.
        users.put("analista", new User("analista", PasswordUtil.hashPassword(config.analystPassword()), Role.USER));
    }

    // Busca un usuario por nombre.
    // `Optional` se usa para representar "puede existir o puede no existir".
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    // Intenta autenticar comparando usuario y contrasena.
    public Optional<User> authenticate(String username, String password) {
        // Busca el usuario en el mapa.
        User user = users.get(username);
        // Si no existe, devuelve vacio.
        if (user == null) {
            return Optional.empty();
        }
        // Verifica la contrasena ingresada contra el hash guardado.
        // El operador ternario tiene esta forma:
        // condicion ? valor_si_true : valor_si_false
        return PasswordUtil.verifyPassword(password, user.passwordHash()) ? Optional.of(user) : Optional.empty();
    }
}
