package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.http.ApiException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC hacia MySQL. Los datos de conexión vienen del
 * archivo config.properties (ya no están quemados en el código, como en
 * ConexionBD de la AA2-EV01).
 *
 * Limitación conocida: abre una conexión por operación. Para mayor carga se
 * recomienda un pool de conexiones (por ejemplo HikariCP).
 */
public class ConexionMySQL {

    private final String url;
    private final String usuario;
    private final String contrasena;

    public ConexionMySQL(String url, String usuario, String contrasena) {
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public Connection obtener() {
        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            System.err.println("[BD] No fue posible conectar con MySQL: " + e.getMessage());
            throw new ApiException(503, "BASE_DATOS_NO_DISPONIBLE",
                    "La base de datos no está disponible en este momento. Intente más tarde.");
        }
    }

    /** Verifica la conexión al iniciar el servidor (lanza SQLException si falla). */
    public void probar() throws SQLException {
        Connection c = DriverManager.getConnection(url, usuario, contrasena);
        c.close();
    }

    /** Convierte un error SQL inesperado en una excepción no verificada. */
    static RuntimeException error(String operacion, SQLException e) {
        return new IllegalStateException("Error de base de datos al " + operacion + ": " + e.getMessage(), e);
    }

    static void cerrar(AutoCloseable recurso) {
        if (recurso != null) {
            try {
                recurso.close();
            } catch (Exception ignorada) {
                // El cierre no debe ocultar el error original.
            }
        }
    }
}
