package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import org.junit.Test;

import java.math.BigDecimal;

import static co.sena.raspicholados.api.Datos.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Pruebas del componente de validación de datos de entrada (capa de servicio). */
public class ValidadorTest {

    @Test
    public void textoValidoSeRecortaYSeAcepta() {
        Validador v = new Validador(json("{'nombre':'  Pulpa de mora  '}"));
        assertEquals("Pulpa de mora", v.texto("nombre", true, 2, 80));
        assertFalse(v.tieneErrores());
    }

    @Test
    public void textoObligatorioAusenteGeneraError() {
        Validador v = new Validador(json("{}"));
        assertNull(v.texto("nombre", true, 2, 80));
        assertTrue(v.tieneErrores());
    }

    @Test
    public void textoMasLargoQueElMaximoGeneraError() {
        Validador v = new Validador(json("{'unidad':'abcdefghijklmnopqrstuvwxyz'}"));
        v.texto("unidad", true, 1, 20);
        assertTrue(v.tieneErrores());
    }

    @Test
    public void enteroRechazaDecimales() {
        Validador v = new Validador(json("{'stockMinimo':2.5}"));
        assertNull(v.entero("stockMinimo", true, 0, 100));
        assertTrue(v.tieneErrores());
    }

    @Test
    public void enteroRechazaTextoEntreComillas() {
        Validador v = new Validador(json("{'cantidad':'10'}"));
        assertNull(v.entero("cantidad", true, 1, 100));
        assertTrue(v.tieneErrores());
    }

    @Test
    public void dineroNegativoGeneraError() {
        Validador v = new Validador(json("{'precioUnitario':-5}"));
        assertNull(v.dinero("precioUnitario", true));
        assertTrue(v.tieneErrores());
    }

    @Test
    public void dineroValidoQuedaConDosDecimales() {
        Validador v = new Validador(json("{'precioUnitario':12500.5}"));
        assertEquals(new BigDecimal("12500.50"), v.dinero("precioUnitario", true));
    }

    @Test
    public void opcionNoDistingueMayusculas() {
        Validador v = new Validador(json("{'tipo':'salida'}"));
        assertEquals("SALIDA", v.opcion("tipo", true, "ENTRADA", "SALIDA"));
    }

    @Test
    public void validarLanza400ConUnDetallePorCampo() {
        Validador v = new Validador(json("{'stockMinimo':2.5,'precioUnitario':-5}"));
        v.texto("nombre", true, 2, 80);
        v.entero("stockMinimo", true, 0, 100);
        v.dinero("precioUnitario", true);
        try {
            v.validar();
            fail("Debía lanzar ApiException");
        } catch (ApiException e) {
            assertEquals(400, e.getEstado());
            assertEquals("VALIDACION_FALLIDA", e.getCodigo());
            assertEquals(3, e.getDetalles().size());
        }
    }
}
