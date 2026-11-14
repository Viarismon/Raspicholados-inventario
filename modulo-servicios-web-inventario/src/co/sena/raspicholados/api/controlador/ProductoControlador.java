package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import co.sena.raspicholados.api.servicio.ProductoServicio;

/**
 * CRUD de productos.
 *   GET    /api/productos?categoria=&buscar=&proveedorId=&stockBajo=true  (autenticada)
 *   GET    /api/productos/{id}   (autenticada)
 *   POST   /api/productos        (administrador)
 *   PUT    /api/productos/{id}   (administrador)
 *   DELETE /api/productos/{id}   (administrador, baja lógica)
 */
public class ProductoControlador {

    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/api/productos", Seguridad.AUTENTICADA, this::listar);
        router.get("/api/productos/{id}", Seguridad.AUTENTICADA, this::obtener);
        router.post("/api/productos", Seguridad.ADMINISTRADOR, this::crear);
        router.put("/api/productos/{id}", Seguridad.ADMINISTRADOR, this::actualizar);
        router.delete("/api/productos/{id}", Seguridad.ADMINISTRADOR, this::eliminar);
    }

    private Respuesta listar(Solicitud s) {
        FiltroProductos filtro = new FiltroProductos();
        filtro.categoria = s.consulta("categoria");
        filtro.texto = s.consulta("buscar");
        filtro.proveedorId = s.consultaLong("proveedorId");
        filtro.soloStockBajo = s.consultaBooleana("stockBajo");
        return Respuesta.ok(servicio.listar(filtro));
    }

    private Respuesta obtener(Solicitud s) {
        return Respuesta.ok(servicio.obtener(s.idRuta("id")));
    }

    private Respuesta crear(Solicitud s) throws Exception {
        Producto p = servicio.crear(s.cuerpoJson(), s.getUsuario());
        return Respuesta.creado("Producto creado correctamente", p, "/api/productos/" + p.getId());
    }

    private Respuesta actualizar(Solicitud s) throws Exception {
        return Respuesta.okConMensaje("Producto actualizado correctamente",
                servicio.actualizar(s.idRuta("id"), s.cuerpoJson()));
    }

    private Respuesta eliminar(Solicitud s) {
        servicio.eliminar(s.idRuta("id"));
        return Respuesta.okConMensaje("Producto dado de baja correctamente (su historial se conserva)", null);
    }
}
