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
 * Controlador del endpoint POST /api/registro.
 *
 * Cuerpo esperado (JSON): {"usuario":"...", "contrasena":"..."}
 *
 * Respuestas:
 *   201 Created  -> {"mensaje":"Usuario registrado exitosamente"}
 *   409 Conflict -> {"error":"El usuario ya existe"}
 *   400 Bad Request -> {"error":"Debe enviar usuario y contraseña"}
 *   405 Method Not Allowed -> si no se usa POST
 */
public class RegistroHandler implements HttpHandler {

    private final AutenticacionServicio servicio;

    public RegistroHandler(AutenticacionServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Solo se acepta el método POST para esta operación.
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, JsonUtil.construirRespuesta(
                    "error", "Método no permitido, use POST"));
            return;
        }

        // Se lee y parsea el cuerpo de la petición.
        String cuerpo = leerCuerpo(exchange);
        Map<String, String> datos = JsonUtil.parsearObjetoPlano(cuerpo);
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");

        Resultado resultado = servicio.registrar(usuario, contrasena);

        switch (resultado) {
            case EXITO:
                enviarRespuesta(exchange, 201, JsonUtil.construirRespuesta(
                        "mensaje", "Usuario registrado exitosamente"));
                break;
            case USUARIO_YA_EXISTE:
                enviarRespuesta(exchange, 409, JsonUtil.construirRespuesta(
                        "error", "El usuario ya existe"));
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
