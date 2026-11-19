package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static co.sena.raspicholados.api.Datos.EMPLEADO;
import static co.sena.raspicholados.api.Datos.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** Pruebas de reportes (HU-04 alertas de stock bajo) y de categorías. */
public class ReporteYCategoriaServicioTest {

    private EntornoMemoria e;

    @Before
    public void prepararBaseNueva() {
        e = new EntornoMemoria();
    }

    @Test
    public void lasAlertasIncluyenAgotadosYBajosConElAgotadoPrimero() {
        // Datos de ejemplo: Helado de chocolate 0/5 (agotado), Jarabe de mango 3/4 y Cucharas 90/100 (bajos).
        List<Map<String, Object>> alertas = e.servicioReportes.alertasStockBajo();
        assertEquals(3, alertas.size());
        assertEquals("AGOTADO", alertas.get(0).get("estadoStock"));
    }

    @Test
    public void unaSalidaQueDejaStockBajoApareceEnLasAlertas() {
        e.servicioMovimientos.registrar(json("{'productoId':1,'tipo':'SALIDA','cantidad':45}"), EMPLEADO);
        assertEquals(4, e.servicioReportes.alertasStockBajo().size());
    }

    @Test
    public void elResumenCuentaProductosYUnidades() {
        Map<String, Object> r = e.servicioReportes.resumen();
        assertEquals(9, r.get("totalProductosActivos"));
        assertEquals(1, r.get("productosAgotados"));
        assertEquals(2, r.get("productosStockBajo"));
    }

    @Test
    public void noPermiteCategoriasDuplicadas() {
        try {
            e.servicioCategorias.crear(json("{'nombre':'helados','descripcion':'Repetida'}"));
            fail("Debía detectar el duplicado");
        } catch (ApiException ex) {
            assertEquals(409, ex.getEstado());
        }
    }

    @Test
    public void noPermiteEliminarUnaCategoriaConProductos() {
        try {
            e.servicioCategorias.eliminar(2L); // Helados
            fail("Debía responder 409");
        } catch (ApiException ex) {
            assertEquals("CATEGORIA_CON_PRODUCTOS", ex.getCodigo());
        }
    }
}
