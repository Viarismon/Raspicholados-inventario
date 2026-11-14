package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.modelo.Proveedor;
import co.sena.raspicholados.api.servicio.ProveedorServicio;

/**
 * CRUD de proveedores.
 *   GET    /api/proveedores        (autenticada)
 *   GET    /api/proveedores/{id}   (autenticada)
 *   POST   /api/proveedores        (administrador)
 *   PUT    /api/proveedores/{id}   (administrador)
 *   DELETE /api/proveedores/{id}   (administrador)
 */
public class ProveedorControlador {

    private final ProveedorServicio servicio;

    public ProveedorControlador(ProveedorServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/api/proveedores", Seguridad.AUTENTICADA, this::listar);
        router.get("/api/proveedores/{id}", Seguridad.AUTENTICADA, this::obtener);
        router.post("/api/proveedores", Seguridad.ADMINISTRADOR, this::crear);
        router.put("/api/proveedores/{id}", Seguridad.ADMINISTRADOR, this::actualizar);
        router.delete("/api/proveedores/{id}", Seguridad.ADMINISTRADOR, this::eliminar);
    }

    private Respuesta listar(Solicitud s) {
        return Respuesta.ok(servicio.listar());
    }

    private Respuesta obtener(Solicitud s) {
        return Respuesta.ok(servicio.obtener(s.idRuta("id")));
    }

    private Respuesta crear(Solicitud s) throws Exception {
        Proveedor p = servicio.crear(s.cuerpoJson());
        return Respuesta.creado("Proveedor creado correctamente", p, "/api/proveedores/" + p.getId());
    }

    private Respuesta actualizar(Solicitud s) throws Exception {
        return Respuesta.okConMensaje("Proveedor actualizado correctamente",
                servicio.actualizar(s.idRuta("id"), s.cuerpoJson()));
    }

    private Respuesta eliminar(Solicitud s) {
        servicio.eliminar(s.idRuta("id"));
        return Respuesta.okConMensaje("Proveedor eliminado correctamente", null);
    }
}
