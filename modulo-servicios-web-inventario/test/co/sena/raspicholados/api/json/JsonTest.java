package co.sena.raspicholados.api.json;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Pruebas del componente propio de lectura y escritura de JSON. */
public class JsonTest {

    @Test
    @SuppressWarnings("unchecked")
    public void analizaObjetosListasYNumeros() {
        Map<String, Object> m = (Map<String, Object>) Json.analizar(
                "{\"nombre\":\"Helado\",\"stock\":12,\"precio\":18000.5,\"activo\":true,\"tags\":[\"a\",\"b\"]}");
        assertEquals("Helado", m.get("nombre"));
        assertEquals(new BigDecimal("12"), m.get("stock"));
        assertEquals(new BigDecimal("18000.5"), m.get("precio"));
        assertEquals(Boolean.TRUE, m.get("activo"));
        assertEquals(2, ((List<Object>) m.get("tags")).size());
    }

    @Test
    public void respetaTildesYEscapes() {
        assertEquals("Cereza en almíbar \"roja\"", Json.analizar("\"Cereza en alm\\u00edbar \\\"roja\\\"\""));
    }

    @Test(expected = JsonException.class)
    public void rechazaJsonMalFormado() {
        Json.analizar("{\"nombre\": \"sin cerrar\"");
    }

    @Test(expected = JsonException.class)
    public void rechazaTextoSobranteDespuesDelValor() {
        Json.analizar("{} basura");
    }

    @Test
    public void escribeYVuelveALeerElMismoContenido() {
        Map<String, Object> original = new LinkedHashMap<String, Object>();
        original.put("mensaje", "Línea 1\nLínea \"2\"");
        original.put("cantidad", 30);
        String texto = Json.escribirCompacto(original);
        assertTrue(texto.contains("\\n"));
        @SuppressWarnings("unchecked")
        Map<String, Object> leido = (Map<String, Object>) Json.analizar(texto);
        assertEquals("Línea 1\nLínea \"2\"", leido.get("mensaje"));
        assertEquals(new BigDecimal("30"), leido.get("cantidad"));
    }
}
