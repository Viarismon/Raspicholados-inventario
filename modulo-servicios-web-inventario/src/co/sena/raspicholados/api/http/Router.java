package co.sena.raspicholados.api.http;

import co.sena.raspicholados.api.json.Json;
import co.sena.raspicholados.api.seguridad.JwtUtil;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enrutador central de la API. Se registra una sola vez en el HttpServer
 * (contexto "/") y decide qué controlador atiende cada petición.
 *
 * Responsabilidades transversales que resuelve en un solo lugar:
 *   1. Coincidencia EXACTA de rutas con parámetros ({id}); así /api/loginXYZ
 *      ya no se atiende como /api/login (hallazgo H-05).
 *   2. 404 y 405 siempre en JSON (H-08) y 405 con la cabecera Allow (H-07).
 *   3. Autenticación con token JWT y autorización por rol.
 *   4. Validación de Content-Type en POST/PUT (415).
 *   5. Cabeceras CORS, para que el front-end (AA4-EV03) pueda consumir la API.
 *   6. Conversión de cualquier excepción en una respuesta JSON uniforme.
 *   7. Registro (log) de cada petición con su duración.
 */
public class Router implements HttpHandler {

    private final List<Ruta> rutas = new ArrayList<Ruta>();
    private final JwtUtil jwt;

    public Router(JwtUtil jwt) {
        this.jwt = jwt;
    }

    // Registro de rutas -------------------------------------------------------

    public void get(String patron, Seguridad seguridad, Manejador manejador) {
        rutas.add(new Ruta("GET", patron, seguridad, manejador));
    }

    public void post(String patron, Seguridad seguridad, Manejador manejador) {
        rutas.add(new Ruta("POST", patron, seguridad, manejador));
    }

    public void put(String patron, Seguridad seguridad, Manejador manejador) {
        rutas.add(new Ruta("PUT", patron, seguridad, manejador));
    }

    public void delete(String patron, Seguridad seguridad, Manejador manejador) {
        rutas.add(new Ruta("DELETE", patron, seguridad, manejador));
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    // Atención de peticiones ----------------------------------------------------

    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        long inicio = System.currentTimeMillis();
        String metodo = intercambio.getRequestMethod().toUpperCase();
        String ruta = normalizar(intercambio.getRequestURI().getPath());
        int estado;
        try {
            agregarCabecerasCors(intercambio);
            if ("OPTIONS".equals(metodo)) {
                // Petición de verificación previa (preflight) del navegador.
                estado = 204;
                intercambio.sendResponseHeaders(204, -1);
            } else {
                Respuesta respuesta = despachar(intercambio, metodo, ruta);
                estado = respuesta.getEstado();
                enviar(intercambio, estado, respuesta.getCuerpo(), respuesta.getCabeceras());
            }
        } catch (ApiException e) {
            estado = e.getEstado();
            enviar(intercambio, estado, cuerpoError(e.getEstado(), e.getCodigo(), e.getMessage(),
                    e.getDetalles(), ruta), e.getCabeceras());
        } catch (Exception e) {
            // Error no previsto: se registra completo en consola, pero al cliente
            // solo se le entrega un mensaje genérico (no se filtran detalles internos).
            estado = 500;
            System.err.println("[ERROR] " + metodo + " " + ruta + " -> " + e);
            e.printStackTrace();
            enviar(intercambio, 500, cuerpoError(500, "ERROR_INTERNO",
                    "Ocurrió un error interno en el servidor. Intente de nuevo más tarde.",
                    new ArrayList<Map<String, Object>>(), ruta), new LinkedHashMap<String, String>());
        } finally {
            intercambio.close();
        }
        long duracion = System.currentTimeMillis() - inicio;
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                + "  " + metodo + " " + ruta + "  -> " + estado + "  (" + duracion + " ms)");
    }

    private Respuesta despachar(HttpExchange intercambio, String metodo, String ruta) throws Exception {
        Set<String> metodosPermitidos = new LinkedHashSet<String>();
        for (Ruta candidata : rutas) {
            Map<String, String> parametros = candidata.coincide(ruta);
            if (parametros == null) {
                continue;
            }
            metodosPermitidos.add(candidata.getMetodo());
            if (!candidata.getMetodo().equals(metodo)) {
                continue;
            }
            Solicitud solicitud = new Solicitud(intercambio, parametros);
            autorizar(solicitud, candidata.getSeguridad());
            validarTipoContenido(solicitud);
            return candidata.getManejador().manejar(solicitud);
        }
        if (metodosPermitidos.isEmpty()) {
            throw new ApiException(404, "RUTA_NO_ENCONTRADA",
                    "La ruta " + ruta + " no existe en la API. Consulte GET /api/docs");
        }
        metodosPermitidos.add("OPTIONS");
        String allow = String.join(", ", metodosPermitidos);
        throw new ApiException(405, "METODO_NO_PERMITIDO",
                "El método " + metodo + " no está permitido en " + ruta + ". Métodos permitidos: " + allow)
                .conCabecera("Allow", allow);
    }

    private void autorizar(Solicitud solicitud, Seguridad seguridad) {
        if (seguridad == Seguridad.PUBLICA) {
            return;
        }
        String cabecera = solicitud.getCabecera("Authorization");
        if (cabecera == null || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ApiException(401, "TOKEN_REQUERIDO",
                    "Debe enviar el encabezado Authorization: Bearer <token>. Obtenga el token en POST /api/auth/login")
                    .conCabecera("WWW-Authenticate", "Bearer realm=\"raspicholados\"");
        }
        UsuarioAutenticado usuario = jwt.verificar(cabecera.substring(7).trim());
        if (seguridad == Seguridad.ADMINISTRADOR && !usuario.esAdministrador()) {
            throw new ApiException(403, "PERMISO_DENEGADO",
                    "Esta operación solo la puede realizar un usuario con rol ADMINISTRADOR");
        }
        solicitud.setUsuario(usuario);
    }

    private void validarTipoContenido(Solicitud solicitud) {
        String metodo = solicitud.getMetodo();
        if (!"POST".equals(metodo) && !"PUT".equals(metodo)) {
            return;
        }
        String tipo = solicitud.getCabecera("Content-Type");
        if (tipo == null || !tipo.toLowerCase().startsWith("application/json")) {
            throw new ApiException(415, "TIPO_CONTENIDO_NO_SOPORTADO",
                    "Envíe el cuerpo con el encabezado Content-Type: application/json");
        }
    }

    // Utilidades de respuesta -------------------------------------------------------

    private static String normalizar(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            return "/";
        }
        while (ruta.length() > 1 && ruta.endsWith("/")) {
            ruta = ruta.substring(0, ruta.length() - 1);
        }
        return ruta;
    }

    private static void agregarCabecerasCors(HttpExchange intercambio) {
        intercambio.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        intercambio.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        intercambio.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        intercambio.getResponseHeaders().set("Access-Control-Expose-Headers", "Location, Retry-After");
    }

    static Map<String, Object> cuerpoError(int estado, String codigo, String mensaje,
                                           List<Map<String, Object>> detalles, String ruta) {
        Map<String, Object> error = new LinkedHashMap<String, Object>();
        error.put("estado", estado);
        error.put("codigo", codigo);
        error.put("mensaje", mensaje);
        if (detalles != null && !detalles.isEmpty()) {
            error.put("detalles", detalles);
        }
        error.put("ruta", ruta);
        error.put("fecha", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
        cuerpo.put("error", error);
        return cuerpo;
    }

    private static void enviar(HttpExchange intercambio, int estado, Object cuerpo,
                               Map<String, String> cabeceras) throws IOException {
        for (Map.Entry<String, String> c : cabeceras.entrySet()) {
            intercambio.getResponseHeaders().set(c.getKey(), c.getValue());
        }
        if (cuerpo == null || estado == 204) {
            intercambio.sendResponseHeaders(estado, -1);
            return;
        }
        byte[] bytes = Json.escribir(cuerpo).getBytes("UTF-8");
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, bytes.length);
        OutputStream salida = intercambio.getResponseBody();
        try {
            salida.write(bytes);
        } finally {
            salida.close();
        }
    }
}
