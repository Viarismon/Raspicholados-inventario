package com.raspicholados.auth.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad muy simple para leer y construir JSON, pensada únicamente para
 * los cuerpos planos de este servicio (pares clave-valor de tipo String).
 *
 * No se usó ninguna librería externa (como Gson o Jackson) porque el
 * entorno de compilación de esta evidencia es JDK puro, sin gestor de
 * dependencias (Maven/Gradle). Para un proyecto productivo se recomendaría
 * usar una librería de JSON madura y probada.
 */
public class JsonUtil {

    private static final Pattern PATRON_PAR =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\"");

    /**
     * Parsea un JSON plano de un solo nivel, por ejemplo:
     * {"usuario":"vam","contrasena":"1234"}
     *
     * @param json cadena JSON recibida en el cuerpo de la petición
     * @return mapa con las claves y valores encontrados
     */
    public static Map<String, String> parsearObjetoPlano(String json) {
        Map<String, String> resultado = new HashMap<>();
        if (json == null) {
            return resultado;
        }
        Matcher matcher = PATRON_PAR.matcher(json);
        while (matcher.find()) {
            resultado.put(matcher.group(1), matcher.group(2));
        }
        return resultado;
    }

    /**
     * Construye un JSON plano de un solo campo mensaje/error, por ejemplo:
     * {"mensaje":"Autenticación exitosa"}
     */
    public static String construirRespuesta(String clave, String valor) {
        return "{\"" + clave + "\":\"" + escapar(valor) + "\"}";
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
