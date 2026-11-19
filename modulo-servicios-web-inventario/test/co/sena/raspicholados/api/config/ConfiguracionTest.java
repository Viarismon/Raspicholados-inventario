package co.sena.raspicholados.api.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/** Pruebas de la configuración externa: archivo general, archivo local y variables de entorno. */
public class ConfiguracionTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    private File escribir(String nombre, String contenido) throws IOException {
        File f = new File(carpeta.getRoot(), nombre);
        Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        try {
            w.write(contenido);
        } finally {
            w.close();
        }
        return f;
    }

    @Test
    public void leeElArchivoGeneral() throws IOException {
        File general = escribir("config.properties", "servidor.puerto=9090\nbd.contrasena=\n");
        Configuracion c = new Configuracion(general.getPath(), Collections.<String, String>emptyMap());
        assertEquals(9090, c.entero("servidor.puerto", 8080));
        assertEquals("", c.textoPermitiendoVacio("bd.contrasena", null));
    }

    @Test
    public void elArchivoLocalReemplazaAlGeneral() throws IOException {
        File general = escribir("config.properties", "bd.contrasena=\n");
        escribir("config.local.properties", "bd.contrasena=claveLocal1\n");
        Configuracion c = new Configuracion(general.getPath(), Collections.<String, String>emptyMap());
        assertEquals("claveLocal1", c.textoPermitiendoVacio("bd.contrasena", ""));
    }

    @Test
    public void laVariableDeEntornoTieneLaMayorPrioridad() throws IOException {
        File general = escribir("config.properties", "bd.contrasena=\npersistencia.modo=mysql\n");
        escribir("config.local.properties", "bd.contrasena=claveLocal1\n");
        Map<String, String> entorno = new HashMap<String, String>();
        entorno.put("RASPI_BD_CONTRASENA", "claveEntorno2");
        entorno.put("RASPI_PERSISTENCIA_MODO", "memoria");
        Configuracion c = new Configuracion(general.getPath(), entorno);
        assertEquals("claveEntorno2", c.textoPermitiendoVacio("bd.contrasena", ""));
        assertEquals("memoria", c.texto("persistencia.modo", "mysql"));
    }

    @Test
    public void usaElValorPorDefectoSiNoExisteElArchivo() {
        Configuracion c = new Configuracion(new File(carpeta.getRoot(), "no-existe.properties").getPath(),
                Collections.<String, String>emptyMap());
        assertEquals(8080, c.entero("servidor.puerto", 8080));
    }

    @Test
    public void nombreDeLaVariableDeEntorno() {
        assertEquals("RASPI_BD_CONTRASENA", Configuracion.variableDeEntorno("bd.contrasena"));
    }
}
