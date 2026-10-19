package com.raspicholados.auth.web;

import com.raspicholados.auth.servicio.AutenticacionServicio;
import com.raspicholados.auth.servicio.AutenticacionServicio.Resultado;
import com.raspicholados.auth.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controlador del endpoint POST /api/login.
 *
 * Cuerpo esperado (JSON): {"usuario":"...", "contrasena":"..."}
 *
 * Respuestas:
 *   200 OK -> {"mensaje":"Autenticación exitosa"}
 *   401 Unauthorized -> {"error":"Error en la autenticación"}
 *   400 Bad Request -> {"error":"Debe enviar usuario y contraseña"}
 *   405 Method Not Allowed -> si no se usa POST
 *
 * Nota: se responde siempre con el mismo mensaje de error genérico
 * ("Error en la autenticación") tanto si el usuario no existe como si
 * la contraseña es incorrecta. Esto evita revelar a un atacante si un
 * nombre de usuario específico está o no registrado en el sistema
 * (enumeración de usuarios), una práctica recomendada por OWASP (2021)
 * en su guía de autenticación segura.
 */
public class LoginHandler implements HttpHandler {

    private final AutenticacionServicio servicio;

    public LoginHandler(AutenticacionServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, JsonUtil.construirRespuesta(
                    "error", "Método no permitido, use POST"));
            return;
        }

        String cuerpo = leerCuerpo(exchange);
        Map<String, String> datos = JsonUtil.parsearObjetoPlano(cuerpo);
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");

        Resultado resultado = servicio.autenticar(usuario, contrasena);

        switch (resultado) {
            case EXITO:
                enviarRespuesta(exchange, 200, JsonUtil.construirRespuesta(
                        "mensaje", "Autenticación exitosa"));
                break;
            case CREDENCIALES_INVALIDAS:
                enviarRespuesta(exchange, 401, JsonUtil.construirRespuesta(
                        "error", "Error en la autenticación"));
                break;
            default: // DATOS_INCOMPLETOS
                enviarRespuesta(exchange, 400, JsonUtil.construirRespuesta(
                        "error", "Debe enviar usuario y contraseña"));
        }
    }

    private String leerCuerpo(HttpExchange exchange) throws IOException {
        // Lectura compatible con Java 8: InputStream.readAllBytes() solo
        // existe desde Java 9, por eso se lee el cuerpo en bloques.
        InputStream entrada = exchange.getRequestBody();
        ByteArrayOutputStream acumulador = new ByteArrayOutputStream();
        byte[] bloque = new byte[1024];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            acumulador.write(bloque, 0, leidos);
        }
        return new String(acumulador.toByteArray(), StandardCharsets.UTF_8);
    }

    private void enviarRespuesta(HttpExchange exchange, int codigoEstado, String cuerpoJson)
            throws IOException {
        byte[] bytesRespuesta = cuerpoJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigoEstado, bytesRespuesta.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(bytesRespuesta);
        }
    }
}
