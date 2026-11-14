package co.sena.raspicholados.api.http;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respuesta HTTP que devuelve un controlador. Encapsula el código de estado,
 * el cuerpo (que se serializa a JSON) y las cabeceras adicionales.
 *
 * Convención de cuerpos exitosos de la API:
 *   un recurso       -> { "datos": { ... } }
 *   una colección    -> { "datos": [ ... ], "total": n }
 *   creación (201)   -> { "mensaje": "...", "datos": { ... } } + cabecera Location
 */
public class Respuesta {

    private final int estado;
    private final Object cuerpo;
    private final Map<String, String> cabeceras = new LinkedHashMap<String, String>();

    private Respuesta(int estado, Object cuerpo) {
        this.estado = estado;
        this.cuerpo = cuerpo;
    }

    /** 200 OK con un recurso o una colección envueltos en "datos". */
    public static Respuesta ok(Object datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
        cuerpo.put("datos", datos);
        if (datos instanceof Collection) {
            cuerpo.put("total", ((Collection<?>) datos).size());
        }
        return new Respuesta(200, cuerpo);
    }

    /** 200 OK con un mensaje y, opcionalmente, datos. */
    public static Respuesta okConMensaje(String mensaje, Object datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
        cuerpo.put("mensaje", mensaje);
        if (datos != null) {
            cuerpo.put("datos", datos);
        }
        return new Respuesta(200, cuerpo);
    }

    /** 200 OK con un cuerpo libre (se usa para la especificación OpenAPI). */
    public static Respuesta cuerpoLibre(Object cuerpo) {
        return new Respuesta(200, cuerpo);
    }

    /** 201 Created con la ubicación del nuevo recurso. */
    public static Respuesta creado(String mensaje, Object datos, String ubicacion) {
        Map<String, Object> cuerpo = new LinkedHashMap<String, Object>();
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("datos", datos);
        Respuesta r = new Respuesta(201, cuerpo);
        r.cabeceras.put("Location", ubicacion);
        return r;
    }

    public int getEstado() {
        return estado;
    }

    public Object getCuerpo() {
        return cuerpo;
    }

    public Map<String, String> getCabeceras() {
        return cabeceras;
    }
}
