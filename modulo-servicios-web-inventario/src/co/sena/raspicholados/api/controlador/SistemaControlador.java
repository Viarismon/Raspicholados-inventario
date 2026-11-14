package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Ruta;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.json.Json;
import co.sena.raspicholados.api.util.Fechas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicios técnicos públicos.
 *   GET /api             -> índice de rutas disponibles
 *   GET /api/salud       -> estado del servicio (monitoreo)
 *   GET /api/docs        -> especificación OpenAPI 3 (docs/openapi.json)
 */
public class SistemaControlador {

    private final String modoPersistencia;
    private final long inicio = System.currentTimeMillis();
    private Router router;

    public SistemaControlador(String modoPersistencia) {
        this.modoPersistencia = modoPersistencia;
    }

    public void registrarRutas(Router router) {
        this.router = router;
        router.get("/api", Seguridad.PUBLICA, this::indice);
        router.get("/api/salud", Seguridad.PUBLICA, this::salud);
        router.get("/api/docs", Seguridad.PUBLICA, this::docs);
    }

    private Respuesta indice(Solicitud s) {
        List<Map<String, Object>> rutas = new ArrayList<Map<String, Object>>();
        for (Ruta r : router.getRutas()) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("metodo", r.getMetodo());
            m.put("ruta", r.getPatron());
            m.put("acceso", r.getSeguridad().name());
            rutas.add(m);
        }
        return Respuesta.ok(rutas);
    }

    private Respuesta salud(Solicitud s) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("estado", "OK");
        m.put("servicio", "API de inventario Raspicholados");
        m.put("version", "1.0.0");
        m.put("persistencia", modoPersistencia);
        m.put("segundosEnLinea", (System.currentTimeMillis() - inicio) / 1000);
        m.put("fecha", Fechas.ahora());
        return Respuesta.ok(m);
    }

    private Respuesta docs(Solicitud s) throws IOException {
        File archivo = new File("docs/openapi.json");
        if (!archivo.exists()) {
            throw new ApiException(404, "DOCUMENTACION_NO_ENCONTRADA",
                    "No se encontró docs/openapi.json. Ejecute el servidor desde la carpeta del módulo.");
        }
        InputStream entrada = new FileInputStream(archivo);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] bloque = new byte[8192];
            int n;
            while ((n = entrada.read(bloque)) != -1) {
                bytes.write(bloque, 0, n);
            }
            return Respuesta.cuerpoLibre(Json.analizar(new String(bytes.toByteArray(), "UTF-8")));
        } finally {
            entrada.close();
        }
    }
}
