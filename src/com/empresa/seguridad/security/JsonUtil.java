package com.empresa.seguridad.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Utilidades simples para leer y construir JSON.
// Nota: para proyectos grandes se suele usar una libreria como Jackson o Gson.
public final class JsonUtil {
    private JsonUtil() {
    }

    // Lee un JSON "plano" como {"campo":"valor"} y lo convierte en mapa.
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        while (matcher.find()) {
            values.put(matcher.group(1), matcher.group(2));
        }
        return values;
    }

    // Busca un campo de texto dentro de un JSON.
    public static String readString(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Busca un campo numerico entero largo dentro de un JSON.
    public static long readLong(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : -1L;
    }

    // Construye un JSON simple con una sola clave y un solo valor.
    public static String jsonMessage(String key, String value) {
        return "{\"" + key + "\":\"" + escape(value) + "\"}";
    }

    // Devuelve un JSON con el token generado en login.
    public static String jsonToken(String token) {
        return "{\"token\":\"" + escape(token) + "\"}";
    }

    // Convierte una lista de clientes a JSON.
    public static String customerListJson(Iterable<com.empresa.seguridad.model.Customer> customers) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean first = true;
        for (com.empresa.seguridad.model.Customer customer : customers) {
            if (!first) {
                builder.append(",");
            }
            builder.append("{\"id\":").append(customer.id())
                    .append(",\"name\":\"").append(escape(customer.name()))
                    .append("\",\"email\":\"").append(escape(customer.email()))
                    .append("\"}");
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    // Convierte un solo cliente a JSON.
    public static String customerJson(com.empresa.seguridad.model.Customer customer) {
        return "{\"id\":" + customer.id() + ",\"name\":\"" + escape(customer.name()) + "\",\"email\":\"" + escape(customer.email()) + "\"}";
    }

    // Escapa caracteres especiales para no romper el JSON.
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
