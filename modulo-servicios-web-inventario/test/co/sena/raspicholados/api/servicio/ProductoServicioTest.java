package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import org.junit.Before;
import org.junit.Test;

import static co.sena.raspicholados.api.Datos.ADMIN;
import static co.sena.raspicholados.api.Datos.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/** Pruebas del módulo de productos (HU-01: registrar un producto nuevo). */
public class ProductoServicioTest {

    private EntornoMemoria e;

    @Before
    public void prepararBaseNueva() {
        e = new EntornoMemoria();
    }

    @Test
    public void creaUnProductoConStockInicialYRegistraLaEntrada() {
        Producto p = e.servicioProductos.crear(json("{'nombre':'Pulpa de maracuyá','categoria':'insumos',"
                + "'unidadMedida':'Kilogramo','stockMinimo':8,'precioUnitario':12500.5,'proveedorId':1,'stockInicial':25}"), ADMIN);
        assertEquals(25, p.getStockActual());
        assertEquals("Insumos", p.getCategoria()); // corrige mayúsculas con el nombre oficial
        assertEquals(Producto.ESTADO_NORMAL, p.getEstadoStock());
        assertEquals(1, e.servicioMovimientos.listar(p.getId(), null, null, null, null).size());
    }

    @Test
    public void rechazaUnProductoConCategoriaInexistenteYDatosInvalidos() {
        try {
            e.servicioProductos.crear(json("{'nombre':'Producto malo','categoria':'Inexistente',"
                    + "'unidadMedida':'Unidad','stockMinimo':2.5,'precioUnitario':-5}"), ADMIN);
            fail("Debía rechazar el producto");
        } catch (ApiException ex) {
            assertEquals(400, ex.getEstado());
            assertEquals(3, ex.getDetalles().size());
        }
    }

    @Test
    public void noPermiteDosProductosActivosConElMismoNombre() {
        try {
            e.servicioProductos.crear(json("{'nombre':'Hielo raspado','categoria':'Insumos',"
                    + "'unidadMedida':'Kilogramo','stockMinimo':5,'precioUnitario':800}"), ADMIN);
            fail("Debía detectar el duplicado");
        } catch (ApiException ex) {
            assertEquals(409, ex.getEstado());
            assertEquals("PRODUCTO_DUPLICADO", ex.getCodigo());
        }
    }

    @Test
    public void noPermiteEditarElStockDirectamente() {
        try {
            e.servicioProductos.actualizar(1L, json("{'nombre':'Hielo raspado','categoria':'Insumos',"
                    + "'unidadMedida':'Kilogramo','stockMinimo':10,'precioUnitario':800,'stockActual':999}"));
            fail("Debía rechazar el cambio de stock");
        } catch (ApiException ex) {
            assertEquals(400, ex.getEstado());
        }
    }

    @Test
    public void darDeBajaLoSacaDelListadoDeActivos() {
        int antes = e.servicioProductos.listar(new FiltroProductos()).size();
        e.servicioProductos.eliminar(1L);
        assertEquals(antes - 1, e.servicioProductos.listar(new FiltroProductos()).size());
        assertFalse(e.productos.buscarPorId(1L).isActivo());
    }

    @Test
    public void consultarUnIdInexistenteResponde404() {
        try {
            e.servicioProductos.obtener(9999L);
            fail("Debía responder 404");
        } catch (ApiException ex) {
            assertEquals(404, ex.getEstado());
        }
    }
}
