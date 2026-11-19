package co.sena.raspicholados.api;

import co.sena.raspicholados.api.json.Json;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;

import java.util.Map;

/** Utilidades compartidas por las pruebas unitarias. */
public final class Datos {

    public static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(1L, "admin", "ADMINISTRADOR");
    public static final UsuarioAutenticado EMPLEADO = new UsuarioAutenticado(2L, "laura", "EMPLEADO");

    private Datos() {
    }

    /** Convierte un texto JSON en el mapa que recibiría un servicio desde la API. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> json(String texto) {
        return (Map<String, Object>) Json.analizar(texto.replace('\'', '"'));
    }
}
