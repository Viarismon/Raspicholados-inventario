package co.sena.raspicholados.inventario.dao;

import co.sena.raspicholados.inventario.conexion.ConexionBD;
import co.sena.raspicholados.inventario.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo: Inventario
 * Propósito: Implementación JDBC de las operaciones CRUD sobre la
 *            tabla producto. Cada método abre su propia conexión y
 *            la cierra al finalizar, evitando fugas de recursos.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
public class ProductoDAOImpl implements ProductoDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO producto (nombre_producto, categoria, unidad_medida, stock_actual, stock_minimo) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_CONSULTAR_POR_ID =
            "SELECT id_producto, nombre_producto, categoria, unidad_medida, stock_actual, stock_minimo " +
            "FROM producto WHERE id_producto = ?";

    private static final String SQL_LISTAR =
            "SELECT id_producto, nombre_producto, categoria, unidad_medida, stock_actual, stock_minimo " +
            "FROM producto ORDER BY nombre_producto";

    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET nombre_producto = ?, categoria = ?, unidad_medida = ?, " +
            "stock_actual = ?, stock_minimo = ? WHERE id_producto = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM producto WHERE id_producto = ?";

    /**
     * Inserta un nuevo producto en el inventario (RF-01).
     */
    @Override
    public boolean insertarProducto(Producto producto) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR)) {

            sentencia.setString(1, producto.getNombreProducto());
            sentencia.setString(2, producto.getCategoria());
            sentencia.setString(3, producto.getUnidadMedida());
            sentencia.setInt(4, producto.getStockActual());
            sentencia.setInt(5, producto.getStockMinimo());

            int filasAfectadas = sentencia.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException excepcionSql) {
            System.err.println("Error al insertar el producto: " + excepcionSql.getMessage());
            return false;
        }
    }

    /**
     * Consulta un producto específico por su identificador (RF-05).
     */
    @Override
    public Producto consultarProductoPorId(int idProducto) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_POR_ID)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return mapearProducto(resultado);
                }
            }

        } catch (SQLException excepcionSql) {
            System.err.println("Error al consultar el producto: " + excepcionSql.getMessage());
        }
        return null;
    }

    /**
     * Lista todos los productos registrados en el inventario (RF-05).
     */
    @Override
    public List<Producto> listarProductos() {
        List<Producto> listaProductos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                listaProductos.add(mapearProducto(resultado));
            }

        } catch (SQLException excepcionSql) {
            System.err.println("Error al listar los productos: " + excepcionSql.getMessage());
        }
        return listaProductos;
    }

    /**
     * Actualiza los datos de un producto existente (equivalente a
     * un ajuste de entradas o salidas de inventario, RF-02 y RF-03).
     */
    @Override
    public boolean actualizarProducto(Producto producto) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            sentencia.setString(1, producto.getNombreProducto());
            sentencia.setString(2, producto.getCategoria());
            sentencia.setString(3, producto.getUnidadMedida());
            sentencia.setInt(4, producto.getStockActual());
            sentencia.setInt(5, producto.getStockMinimo());
            sentencia.setInt(6, producto.getIdProducto());

            int filasAfectadas = sentencia.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException excepcionSql) {
            System.err.println("Error al actualizar el producto: " + excepcionSql.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto del inventario según su identificador.
     */
    @Override
    public boolean eliminarProducto(int idProducto) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {

            sentencia.setInt(1, idProducto);

            int filasAfectadas = sentencia.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException excepcionSql) {
            System.err.println("Error al eliminar el producto: " + excepcionSql.getMessage());
            return false;
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Producto.
     */
    private Producto mapearProducto(ResultSet resultado) throws SQLException {
        return new Producto(
                resultado.getInt("id_producto"),
                resultado.getString("nombre_producto"),
                resultado.getString("categoria"),
                resultado.getString("unidad_medida"),
                resultado.getInt("stock_actual"),
                resultado.getInt("stock_minimo")
        );
    }
}
