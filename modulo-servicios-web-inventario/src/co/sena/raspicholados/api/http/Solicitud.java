package co.sena.raspicholados.api.http;

import co.sena.raspicholados.api.json.Json;
import co.sena.raspicholados.api.json.JsonException;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envoltura de la petición HTTP que reciben los controladores. Ofrece
 * acceso cómodo y validado a:
 *   - parámetros de ruta   (/api/productos/{id})
 *   - parámetros de consulta (?categoria=Helados)
 *   - cuerpo JSON
 *   - usuario autenticado (extraído del token JWT)
 */
public class Solicitud {

    /** Tamaño máximo del cuerpo aceptado: 1 MB. Evita abusos de memoria. */
    public static final int TAMANO_MAXIMO_CUERPO = 1024 * 1024;

    private final HttpExchange intercambio;
    private final Map<String, String> parametrosRuta;
    private final Map<String, String> parametrosConsulta;
    private UsuarioAutenticado usuario;

    public Solicitud(HttpExchange intercambio, Map<String, String> parametrosRuta) {
        this.intercambio = intercambio;
        this.parametrosRuta = parametrosRuta;
        this.parametrosConsulta = analizarConsulta(intercambio.getRequestURI().getRawQuery());
    }

    public String getMetodo() {
        return intercambio.getRequestMethod().toUpperCase();
    }

    public String getRuta() {
        return intercambio.getRequestURI().getPath();
    }

    public String getCabecera(String nombre) {
        return intercambio.getRequestHeaders().getFirst(nombre);
    }

    public String getIpCliente() {
        return intercambio.getRemoteAddress().getAddress().getHostAddress();
    }

    public UsuarioAutenticado getUsuario() {
        return usuario;
    }

    void setUsuario(UsuarioAutenticado usuario) {
        this.usuario = usuario;
    }

    // ------------------------------------------------------------------
    // Parámetros de ruta y de consulta
    // ------------------------------------------------------------------

    /** Devuelve un parámetro de ruta numérico, o 400 si no es un número válido. */
    public long idRuta(String nombre) {
        String valor = parametrosRuta.get(nombre);
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO",
                    "El parámetro de ruta '" + nombre + "' debe ser un número entero positivo; se recibió '" + valor + "'");
        }
    }

    public String consulta(String nombre) {
        String valor = parametrosConsulta.get(nombre);
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    public Long consultaLong(String nombre) {
        String valor = consulta(nombre);
        if (valor == null) {
            return null;
        }
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException e) {
            throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO",
                    "El parámetro de consulta '" + nombre + "' debe ser numérico");
        }
    }

    public boolean consultaBooleana(String nombre) {
        String valor = consulta(nombre);
        return "true".equalsIgnoreCase(valor) || "1".equals(valor) || "si".equalsIgnoreCase(valor);
    }

    // ------------------------------------------------------------------
    // Cuerpo JSON
    // ------------------------------------------------------------------

    /**
     * Lee el cuerpo y lo interpreta como un objeto JSON.
     * Distingue tres errores con códigos propios:
     *   CUERPO_VACIO, JSON_MALFORMADO y JSON_NO_ES_OBJETO.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> cuerpoJson() throws IOException {
        String texto = leerCuerpo();
        if (texto.trim().isEmpty()) {
            throw ApiException.solicitudInvalida("CUERPO_VACIO",
                    "La petición debe incluir un cuerpo JSON");
        }
        Object valor;
        try {
            valor = Json.analizar(texto);
        } catch (JsonException e) {
            throw ApiException.solicitudInvalida("JSON_MALFORMADO",
                    "El cuerpo no es JSON válido: " + e.getMessage());
        }
        if (!(valor instanceof Map)) {
            throw ApiException.solicitudInvalida("JSON_NO_ES_OBJETO",
                    "El cuerpo debe ser un objeto JSON ({ ... })");
        }
        return (Map<String, Object>) valor;
    }

    private String leerCuerpo() throws IOException {
        // Se lee por bloques (compatible con Java 8; no se usa readAllBytes).
        InputStream entrada = intercambio.getRequestBody();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] bloque = new byte[4096];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            bytes.write(bloque, 0, leidos);
            if (bytes.size() > TAMANO_MAXIMO_CUERPO) {
                throw new ApiException(413, "CUERPO_DEMASIADO_GRANDE",
                        "El cuerpo supera el máximo permitido de 1 MB");
            }
        }
        return new String(bytes.toByteArray(), "UTF-8");
    }

    private static Map<String, String> analizarConsulta(String consultaCruda) {
        if (consultaCruda == null || consultaCruda.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> resultado = new LinkedHashMap<String, String>();
        for (String par : consultaCruda.split("&")) {
            if (par.isEmpty()) {
                continue;
            }
            int igual = par.indexOf('=');
            String clave = igual >= 0 ? par.substring(0, igual) : par;
            String valor = igual >= 0 ? par.substring(igual + 1) : "";
            resultado.put(decodificar(clave), decodificar(valor));
        }
        return resultado;
    }

    private static String decodificar(String texto) {
        try {
            return URLDecoder.decode(texto, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return texto;
        }
    }
}
