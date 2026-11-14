package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.servicio.ReporteServicio;

/**
 * Consultas gerenciales.
 *   GET /api/alertas/stock-bajo  (autenticada) -> productos por reabastecer
 *   GET /api/reportes/resumen    (autenticada) -> indicadores del inventario
 */
public class ReporteControlador {

    private final ReporteServicio servicio;

    public ReporteControlador(ReporteServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/api/alertas/stock-bajo", Seguridad.AUTENTICADA, this::alertas);
        router.get("/api/reportes/resumen", Seguridad.AUTENTICADA, this::resumen);
    }

    private Respuesta alertas(Solicitud s) {
        return Respuesta.ok(servicio.alertasStockBajo());
    }

    private Respuesta resumen(Solicitud s) {
        return Respuesta.ok(servicio.resumen());
    }
}
