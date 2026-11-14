package co.sena.raspicholados.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * Lee la configuración externa del archivo config.properties, para no
 * dejar datos sensibles (contraseña de la base de datos, secreto del JWT)
 * escritos dentro del código fuente.
 */
public class Configuracion {

    private final Properties propiedades = new Properties();

    public Configuracion(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            System.out.println("[CONFIG] No se encontró " + ruta + "; se usan valores por defecto.");
            return;
        }
        try (Reader lector = new InputStreamReader(new FileInputStream(archivo), "UTF-8")) {
            propiedades.load(lector);
        } catch (IOException e) {
            System.err.println("[CONFIG] No se pudo leer " + ruta + ": " + e.getMessage());
        }
    }

    public String texto(String clave, String porDefecto) {
        String valor = propiedades.getProperty(clave);
        return valor == null || valor.trim().isEmpty() ? porDefecto : valor.trim();
    }

    /** Igual que texto() pero conserva el valor vacío (p. ej. contraseña de root sin clave). */
    public String textoPermitiendoVacio(String clave, String porDefecto) {
        String valor = propiedades.getProperty(clave);
        return valor == null ? porDefecto : valor.trim();
    }

    public int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(texto(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    public boolean logico(String clave, boolean porDefecto) {
        return Boolean.parseBoolean(texto(clave, String.valueOf(porDefecto)));
    }
}
