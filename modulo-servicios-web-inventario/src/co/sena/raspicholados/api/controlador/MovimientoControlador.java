package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.servicio.MovimientoServicio;

import java.util.Map;

/**
 * Movimientos de inventario (entradas, salidas y ajustes).
 *   GET  /api/movimientos?productoId=&tipo=&desde=&hasta=&limite=  (autenticada)
 *   GET  /api/movimientos/{id}  (autenticada)
 *   POST /api/movimientos       (autenticada: administrador o empleado)
 *
 * Los movimientos no se editan ni se eliminan: un error se corrige con un
 * movimiento de AJUSTE, como en un libro contable.
 */
public class MovimientoControlador {

    private final MovimientoServicio servicio;

    public MovimientoControlador(MovimientoServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/api/movimientos", Seguridad.AUTENTICADA, this::listar);
        router.get("/api/movimientos/{id}", Seguridad.AUTENTICADA, this::obtener);
        router.post("/api/movimientos", Seguridad.AUTENTICADA, this::registrar);
    }

    private Respuesta listar(Solicitud s) {
        return Respuesta.ok(servicio.listar(s.consultaLong("productoId"), s.consulta("tipo"),
                s.consulta("desde"), s.consulta("hasta"), s.consultaLong("limite")));
    }

    private Respuesta obtener(Solicitud s) {
        return Respuesta.ok(servicio.obtener(s.idRuta("id")));
    }

    private Respuesta registrar(Solicitud s) throws Exception {
        Map<String, Object> resultado = servicio.registrar(s.cuerpoJson(), s.getUsuario());
        Movimiento m = (Movimiento) resultado.get("movimiento");
        return Respuesta.creado("Movimiento de " + m.getTipo() + " registrado correctamente",
                resultado, "/api/movimientos/" + m.getId());
    }
}
