package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.servicio.CategoriaServicio;

/**
 * CRUD de categorías.
 *   GET    /api/categorias        (autenticada)
 *   GET    /api/categorias/{id}   (autenticada)
 *   POST   /api/categorias        (administrador)
 *   PUT    /api/categorias/{id}   (administrador)
 *   DELETE /api/categorias/{id}   (administrador)
 */
public class CategoriaControlador {

    private final CategoriaServicio servicio;

    public CategoriaControlador(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/api/categorias", Seguridad.AUTENTICADA, this::listar);
        router.get("/api/categorias/{id}", Seguridad.AUTENTICADA, this::obtener);
        router.post("/api/categorias", Seguridad.ADMINISTRADOR, this::crear);
        router.put("/api/categorias/{id}", Seguridad.ADMINISTRADOR, this::actualizar);
        router.delete("/api/categorias/{id}", Seguridad.ADMINISTRADOR, this::eliminar);
    }

    private Respuesta listar(Solicitud s) {
        return Respuesta.ok(servicio.listar());
    }

    private Respuesta obtener(Solicitud s) {
        return Respuesta.ok(servicio.obtener(s.idRuta("id")));
    }

    private Respuesta crear(Solicitud s) throws Exception {
        Categoria c = servicio.crear(s.cuerpoJson());
        return Respuesta.creado("Categoría creada correctamente", c, "/api/categorias/" + c.getId());
    }

    private Respuesta actualizar(Solicitud s) throws Exception {
        return Respuesta.okConMensaje("Categoría actualizada correctamente",
                servicio.actualizar(s.idRuta("id"), s.cuerpoJson()));
    }

    private Respuesta eliminar(Solicitud s) {
        servicio.eliminar(s.idRuta("id"));
        return Respuesta.okConMensaje("Categoría eliminada correctamente", null);
    }
}
