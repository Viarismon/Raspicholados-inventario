package co.sena.raspicholados.inventario.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Módulo: Inventario
 * Propósito: Clase utilitaria encargada de establecer y cerrar la
 *            conexión JDBC con la base de datos MySQL del sistema
 *            RASPICHOLADOS.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
public class ConexionBD {

    private static final String URL_BASE_DATOS =
            "jdbc:mysql://localhost:3306/raspicholados_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO_BASE_DATOS = "root";
    private static final String CONTRASENA_BASE_DATOS = "Mara1592";

    /**
     * Abre y retorna una nueva conexión JDBC hacia la base de datos.
     *
     * @return objeto Connection listo para ejecutar sentencias SQL.
     * @throws SQLException si la conexión no puede establecerse.
     */
    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException excepcionDriver) {
            throw new SQLException("No se encontró el driver JDBC de MySQL en el classpath.", excepcionDriver);
        }
        return DriverManager.getConnection(URL_BASE_DATOS, USUARIO_BASE_DATOS, CONTRASENA_BASE_DATOS);
    }

    /**
     * Cierra una conexión JDBC de manera segura, validando que no sea nula.
     *
     * @param conexion la conexión a cerrar.
     */
    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException excepcionCierre) {
                System.err.println("Error al cerrar la conexión: " + excepcionCierre.getMessage());
            }
        }
    }
}
