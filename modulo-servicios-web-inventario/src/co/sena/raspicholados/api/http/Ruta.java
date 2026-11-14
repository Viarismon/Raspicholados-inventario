package co.sena.raspicholados.api.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Una ruta registrada en el enrutador: método HTTP + patrón + nivel de
 * seguridad + manejador. El patrón admite segmentos variables entre llaves,
 * por ejemplo "/api/productos/{id}".
 */
public class Ruta {

    private final String metodo;
    private final String patron;
    private final String[] segmentos;
    private final Seguridad seguridad;
    private final Manejador manejador;

    public Ruta(String metodo, String patron, Seguridad seguridad, Manejador manejador) {
        this.metodo = metodo;
        this.patron = patron;
        this.segmentos = patron.split("/");
        this.seguridad = seguridad;
        this.manejador = manejador;
    }

    /**
     * Compara la ruta solicitada con el patrón, segmento por segmento.
     *
     * @return los parámetros extraídos si coincide, o null si no coincide
     */
    public Map<String, String> coincide(String ruta) {
        String[] partes = ruta.split("/");
        if (partes.length != segmentos.length) {
            return null;
        }
        Map<String, String> parametros = new LinkedHashMap<String, String>();
        for (int i = 0; i < segmentos.length; i++) {
            String esperado = segmentos[i];
            if (esperado.startsWith("{") && esperado.endsWith("}")) {
                if (partes[i].isEmpty()) {
                    return null;
                }
                parametros.put(esperado.substring(1, esperado.length() - 1), partes[i]);
            } else if (!esperado.equals(partes[i])) {
                return null;
            }
        }
        return parametros;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getPatron() {
        return patron;
    }

    public Seguridad getSeguridad() {
        return seguridad;
    }

    public Manejador getManejador() {
        return manejador;
    }
}
