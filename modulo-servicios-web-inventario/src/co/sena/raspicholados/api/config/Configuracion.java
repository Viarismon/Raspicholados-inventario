package co.sena.raspicholados.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Lee la configuración externa, para no dejar datos sensibles (contraseña
 * de la base de datos, secreto del JWT) escritos en el código fuente.
 *
 * Orden de prioridad (el último gana):
 *   1. config.properties          -> valores generales (se versiona, SIN contraseñas)
 *   2. config.local.properties    -> valores del equipo (NO se versiona: .gitignore)
 *   3. Variables de entorno        -> RASPI_ + clave en mayúsculas con "_"
 *                                     (ej.: bd.contrasena -> RASPI_BD_CONTRASENA)
 *
 * Así la contraseña de MySQL nunca tiene que escribirse en un archivo que
 * se suba a GitHub (hallazgo 3 de la evidencia AA5-EV04).
 */
public class Configuracion {

    public static final String ARCHIVO_LOCAL = "config.local.properties";

    private final Properties propiedades = new Properties();
    private final Map<String, String> entorno;

    public Configuracion(String ruta) {
        this(ruta, System.getenv());
    }

    /** Constructor que permite indicar las variables de entorno (útil en pruebas unitarias). */
    public Configuracion(String ruta, Map<String, String> entorno) {
        this.entorno = entorno == null ? new HashMap<String, String>() : entorno;
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            System.out.println("[CONFIG] No se encontró " + ruta + "; se usan valores por defecto.");
        } else {
            cargar(archivo);
        }
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        File local = new File(carpeta, ARCHIVO_LOCAL);
        if (local.exists()) {
            cargar(local);
            System.out.println("[CONFIG] Se aplicó " + ARCHIVO_LOCAL + " (configuración local del equipo).");
        }
    }

    private void cargar(File archivo) {
        try (Reader lector = new InputStreamReader(new FileInputStream(archivo), "UTF-8")) {
            propiedades.load(lector);
        } catch (IOException e) {
            System.err.println("[CONFIG] No se pudo leer " + archivo.getName() + ": " + e.getMessage());
        }
    }

    /** Nombre de la variable de entorno que reemplaza a una clave. */
    public static String variableDeEntorno(String clave) {
        return "RASPI_" + clave.toUpperCase().replace('.', '_').replace('-', '_');
    }

    private String crudo(String clave) {
        String desdeEntorno = entorno.get(variableDeEntorno(clave));
        return desdeEntorno != null ? desdeEntorno : propiedades.getProperty(clave);
    }

    public String texto(String clave, String porDefecto) {
        String valor = crudo(clave);
        return valor == null || valor.trim().isEmpty() ? porDefecto : valor.trim();
    }

    /** Igual que texto() pero conserva el valor vacío (p. ej. contraseña de root sin clave). */
    public String textoPermitiendoVacio(String clave, String porDefecto) {
        String valor = crudo(clave);
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
