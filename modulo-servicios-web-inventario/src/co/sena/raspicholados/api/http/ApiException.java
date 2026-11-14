package co.sena.raspicholados.api.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excepción de negocio que el enrutador convierte en una respuesta de error
 * JSON uniforme:
 *
 * <pre>
 * {
 *   "error": {
 *     "estado": 409,
 *     "codigo": "STOCK_INSUFICIENTE",
 *     "mensaje": "...",
 *     "detalles": [ { "campo": "...", "mensaje": "..." } ]
 *   }
 * }
 * </pre>
 *
 * Contar con un código de error legible por máquina (además del código
 * HTTP) permite al cliente distinguir, por ejemplo, un JSON malformado de
 * un dato faltante, ambos 400 (hallazgo H-06 de la evidencia AA5-EV02).
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int estado;
    private final String codigo;
    private final transient List<Map<String, Object>> detalles;
    private final transient Map<String, String> cabeceras = new LinkedHashMap<String, String>();

    public ApiException(int estado, String codigo, String mensaje) {
        this(estado, codigo, mensaje, Collections.<Map<String, Object>>emptyList());
    }

    public ApiException(int estado, String codigo, String mensaje, List<Map<String, Object>> detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = new ArrayList<Map<String, Object>>(detalles);
    }

    /** Agrega una cabecera HTTP a la respuesta de error (p. ej. Allow o Retry-After). */
    public ApiException conCabecera(String nombre, String valor) {
        cabeceras.put(nombre, valor);
        return this;
    }

    public int getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<Map<String, Object>> getDetalles() {
        return detalles;
    }

    public Map<String, String> getCabeceras() {
        return cabeceras;
    }

    // Fábricas de los errores más frecuentes --------------------------------

    public static ApiException noEncontrado(String recurso, Object id) {
        return new ApiException(404, "RECURSO_NO_ENCONTRADO",
                "No existe " + recurso + " con id " + id);
    }

    public static ApiException conflicto(String codigo, String mensaje) {
        return new ApiException(409, codigo, mensaje);
    }

    public static ApiException solicitudInvalida(String codigo, String mensaje) {
        return new ApiException(400, codigo, mensaje);
    }
}
