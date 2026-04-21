package com.empresa.seguridad.service;

import com.empresa.seguridad.model.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// Este servicio administra clientes.
// Otra vez, todo vive en memoria para mantener el ejemplo simple.
public final class CustomerService {
    // `List<Customer>` significa "lista de objetos Customer".
    // Lista segura para varios hilos.
    // `CopyOnWriteArrayList` es una lista pensada para escenarios concurrentes.
    private final List<Customer> customers = new CopyOnWriteArrayList<>();
    // Contador para generar ids nuevos de forma segura.
    // `AtomicInteger` permite aumentar el numero sin pisarse entre hilos.
    private final AtomicInteger ids = new AtomicInteger(2);

    public CustomerService() {
        // Datos iniciales para probar la API sin cargar nada a mano.
        customers.add(new Customer(1, "Globex", "soc@globex.example"));
        customers.add(new Customer(2, "Initech", "security@initech.example"));
    }

    // Devuelve una copia de la lista actual.
    // Se devuelve una copia para no exponer la lista interna.
    public List<Customer> list() {
        return new ArrayList<>(customers);
    }

    // Crea un nuevo cliente, le asigna id y lo guarda.
    public Customer create(String name, String email) {
        Customer customer = new Customer(ids.incrementAndGet(), name, email);
        customers.add(customer);
        return customer;
    }
}
