package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static co.sena.raspicholados.api.Datos.EMPLEADO;
import static co.sena.raspicholados.api.Datos.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Pruebas del módulo de movimientos (HU-02 entradas y HU-03 salidas sin exceder el stock). */
public class MovimientoServicioTest {

    private EntornoMemoria e;

    @Before
    public void prepararBaseNueva() {
        e = new EntornoMemoria();
    }

    @Test
    public void unaEntradaAumentaElStock() {
        // Producto 1 "Hielo raspado" inicia con 50 unidades.
        Map<String, Object> r = e.servicioMovimientos.registrar(
                json("{'productoId':1,'tipo':'ENTRADA','cantidad':10,'motivo':'Compra FV-1021'}"), EMPLEADO);
        Movimiento m = (Movimiento) r.get("movimiento");
        assertEquals(50, m.getStockAnterior());
        assertEquals(60, m.getStockResultante());
        assertEquals(60, e.productos.buscarPorId(1L).getStockActual());
    }

    @Test
    public void unaSalidaDisminuyeElStockYAvisaSiQuedaBajo() {
        Map<String, Object> r = e.servicioMovimientos.registrar(
                json("{'productoId':1,'tipo':'salida','cantidad':45}"), EMPLEADO);
        Producto p = (Producto) r.get("producto");
        assertEquals(5, p.getStockActual());
        assertEquals(Producto.ESTADO_BAJO, p.getEstadoStock());
        assertTrue(String.valueOf(r.get("alerta")).contains("BAJO"));
    }

    @Test
    public void noPermiteSacarMasDeLoDisponible() {
        try {
            e.servicioMovimientos.registrar(json("{'productoId':1,'tipo':'SALIDA','cantidad':100}"), EMPLEADO);
            fail("Debía responder 409");
        } catch (ApiException ex) {
            assertEquals(409, ex.getEstado());
            assertEquals("STOCK_INSUFICIENTE", ex.getCodigo());
        }
        assertEquals(50, e.productos.buscarPorId(1L).getStockActual()); // el stock no cambió
    }

    @Test
    public void rechazaCantidadCeroOTipoDesconocido() {
        try {
            e.servicioMovimientos.registrar(json("{'productoId':1,'tipo':'REGALO','cantidad':0}"), EMPLEADO);
            fail("Debía responder 400");
        } catch (ApiException ex) {
            assertEquals(400, ex.getEstado());
            assertEquals(2, ex.getDetalles().size());
        }
    }

    @Test
    public void unProductoDadoDeBajaNoAdmiteMovimientos() {
        e.servicioProductos.eliminar(2L);
        try {
            e.servicioMovimientos.registrar(json("{'productoId':2,'tipo':'ENTRADA','cantidad':5}"), EMPLEADO);
            fail("Debía responder 409");
        } catch (ApiException ex) {
            assertEquals("PRODUCTO_INACTIVO", ex.getCodigo());
        }
    }

    @Test
    public void elHistorialGuardaElUsuarioQueRegistro() {
        e.servicioMovimientos.registrar(json("{'productoId':1,'tipo':'ENTRADA','cantidad':3}"), EMPLEADO);
        Movimiento m = e.servicioMovimientos.listar(1L, null, null, null, 1L).get(0);
        assertEquals("laura", m.getUsuarioNombre());
    }
}
