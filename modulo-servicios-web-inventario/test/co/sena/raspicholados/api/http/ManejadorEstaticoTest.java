package co.sena.raspicholados.api.http;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Pruebas del componente que sirve la aplicación web (archivos estáticos). */
public class ManejadorEstaticoTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    @Test
    public void laRaizSeTraduceAIndexHtml() throws IOException {
        File raiz = carpeta.newFolder("web");
        assertEquals(new File(raiz, "index.html").getCanonicalPath(),
                ManejadorEstatico.resolver(raiz, "/").getCanonicalPath());
    }

    @Test
    public void resuelveArchivosDentroDeLaRaiz() throws IOException {
        File raiz = carpeta.newFolder("web");
        File archivo = ManejadorEstatico.resolver(raiz, "/js/app.js");
        assertTrue(archivo.getCanonicalPath().endsWith("app.js"));
    }

    @Test
    public void bloqueaElRecorridoDeDirectorios() throws IOException {
        File raiz = carpeta.newFolder("web");
        assertNull(ManejadorEstatico.resolver(raiz, "/../config.properties"));
        assertNull(ManejadorEstatico.resolver(raiz, "/%2e%2e/config.properties"));
        assertNull(ManejadorEstatico.resolver(raiz, "/js/..%2f..%2fconfig.properties"));
        assertNull(ManejadorEstatico.resolver(raiz, "/..\\config.properties"));
    }

    @Test
    public void soloSirveExtensionesPermitidas() {
        assertEquals("text/html; charset=UTF-8", ManejadorEstatico.tipoMime("index.html"));
        assertEquals("application/javascript; charset=UTF-8", ManejadorEstatico.tipoMime("app.js"));
        assertNull(ManejadorEstatico.tipoMime("config.properties"));
        assertNull(ManejadorEstatico.tipoMime("Main.class"));
    }
}
