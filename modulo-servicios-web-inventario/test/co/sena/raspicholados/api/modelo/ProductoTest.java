package co.sena.raspicholados.api.modelo;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/** Pruebas de las reglas del modelo Producto (estado del stock y valor). */
public class ProductoTest {

    private static Producto producto(int stock, int minimo, String precio) {
        Producto p = new Producto();
        p.setStockActual(stock);
        p.setStockMinimo(minimo);
        p.setPrecioUnitario(new BigDecimal(precio));
        return p;
    }

    @Test
    public void sinStockEstaAgotado() {
        assertEquals(Producto.ESTADO_AGOTADO, producto(0, 5, "1000").getEstadoStock());
    }

    @Test
    public void enElMinimoOMenosEstaBajo() {
        assertEquals(Producto.ESTADO_BAJO, producto(5, 5, "1000").getEstadoStock());
        assertEquals(Producto.ESTADO_BAJO, producto(3, 5, "1000").getEstadoStock());
    }

    @Test
    public void porEncimaDelMinimoEstaNormal() {
        assertEquals(Producto.ESTADO_NORMAL, producto(6, 5, "1000").getEstadoStock());
    }

    @Test
    public void elValorDelInventarioEsStockPorPrecio() {
        assertEquals(0, new BigDecimal("300.00").compareTo(producto(25, 8, "12").getValorInventario()));
    }
}
