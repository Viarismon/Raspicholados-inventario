package co.sena.raspicholados.api.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Sirve los archivos de la aplicación web (HTML, CSS, JavaScript) desde la
 * carpeta web/ (evidencia GA8-220501096-AA1-EV01: integración del front-end
 * con la API).
 *
 * Al publicar la página y la API en el mismo origen (http://localhost:8080)
 * el navegador no necesita CORS y el token nunca sale hacia otro dominio.
 *
 * Seguridad aplicada:
 *  - Solo se atienden GET y HEAD.
 *  - Se bloquea el recorrido de directorios ("../"): la ruta pedida debe
 *    quedar dentro de la carpeta raíz (ver {@link #resolver(File, String)}).
 *  - Solo se sirven extensiones conocidas (lista blanca de tipos MIME).
 *  - Cabeceras: Content-Security-Policy, X-Content-Type-Options,
 *    X-Frame-Options y Referrer-Policy.
 */
public class ManejadorEstatico implements HttpHandler {

    static final String POLITICA_CSP = "default-src 'self'; script-src 'self'; "
            + "style-src 'self' https://fonts.googleapis.com; "
            + "font-src 'self' https://fonts.gstatic.com; img-src 'self' data:; "
            + "connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'";

    private static final Map<String, String> TIPOS = new HashMap<String, String>();

    static {
        TIPOS.put("html", "text/html; charset=UTF-8");
        TIPOS.put("css", "text/css; charset=UTF-8");
        TIPOS.put("js", "application/javascript; charset=UTF-8");
        TIPOS.put("json", "application/json; charset=UTF-8");
        TIPOS.put("png", "image/png");
        TIPOS.put("jpg", "image/jpeg");
        TIPOS.put("svg", "image/svg+xml");
        TIPOS.put("ico", "image/x-icon");
    }

    private final File raiz;

    public ManejadorEstatico(File raiz) {
        this.raiz = raiz;
    }

    public File getRaiz() {
        return raiz;
    }

    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        try {
            String metodo = intercambio.getRequestMethod().toUpperCase();
            if (!"GET".equals(metodo) && !"HEAD".equals(metodo)) {
                intercambio.getResponseHeaders().set("Allow", "GET, HEAD");
                enviarTexto(intercambio, 405, "Método no permitido");
                return;
            }
            File archivo = resolver(raiz, intercambio.getRequestURI().getRawPath());
            String tipo = archivo == null ? null : tipoMime(archivo.getName());
            if (archivo == null || !archivo.isFile() || tipo == null) {
                enviarTexto(intercambio, 404, "Recurso no encontrado");
                return;
            }
            byte[] contenido = leer(archivo);
            agregarCabecerasSeguridad(intercambio);
            intercambio.getResponseHeaders().set("Content-Type", tipo);
            intercambio.getResponseHeaders().set("Cache-Control", "no-cache");
            if ("HEAD".equals(metodo)) {
                intercambio.sendResponseHeaders(200, -1);
                return;
            }
            intercambio.sendResponseHeaders(200, contenido.length);
            OutputStream salida = intercambio.getResponseBody();
            try {
                salida.write(contenido);
            } finally {
                salida.close();
            }
        } finally {
            intercambio.close();
        }
    }

    /**
     * Convierte la ruta pedida en un archivo dentro de la carpeta raíz.
     * Devuelve null si la ruta intenta salir de la raíz o no es válida.
     * "/" se traduce a index.html.
     */
    public static File resolver(File raiz, String rutaCruda) {
        if (rutaCruda == null) {
            return null;
        }
        String ruta;
        try {
            ruta = URLDecoder.decode(rutaCruda.replace("+", "%2B"), "UTF-8");
        } catch (Exception e) {
            return null;
        }
        if (ruta.indexOf('\0') >= 0 || ruta.contains("\\")) {
            return null;
        }
        if (ruta.isEmpty() || ruta.endsWith("/")) {
            ruta = ruta + "index.html";
        }
        try {
            String base = raiz.getCanonicalPath();
            File archivo = new File(raiz, ruta.startsWith("/") ? ruta.substring(1) : ruta);
            String canonica = archivo.getCanonicalPath();
            if (!canonica.equals(base) && !canonica.startsWith(base + File.separator)) {
                return null;
            }
            return archivo;
        } catch (IOException e) {
            return null;
        }
    }

    /** Tipo MIME según la extensión; null si la extensión no está permitida. */
    public static String tipoMime(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            return null;
        }
        return TIPOS.get(nombre.substring(punto + 1).toLowerCase());
    }

    private static void agregarCabecerasSeguridad(HttpExchange intercambio) {
        intercambio.getResponseHeaders().set("Content-Security-Policy", POLITICA_CSP);
        intercambio.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        intercambio.getResponseHeaders().set("X-Frame-Options", "DENY");
        intercambio.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    }

    private static void enviarTexto(HttpExchange intercambio, int estado, String texto) throws IOException {
        byte[] bytes = texto.getBytes("UTF-8");
        agregarCabecerasSeguridad(intercambio);
        intercambio.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, bytes.length);
        OutputStream salida = intercambio.getResponseBody();
        try {
            salida.write(bytes);
        } finally {
            salida.close();
        }
    }

    private static byte[] leer(File archivo) throws IOException {
        InputStream entrada = new FileInputStream(archivo);
        try {
            ByteArrayOutputStream memoria = new ByteArrayOutputStream();
            byte[] bloque = new byte[8192];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                memoria.write(bloque, 0, leidos);
            }
            return memoria.toByteArray();
        } finally {
            entrada.close();
        }
    }
}
