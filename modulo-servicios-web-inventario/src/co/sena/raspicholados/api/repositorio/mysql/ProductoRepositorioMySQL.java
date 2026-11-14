package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.util.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del repositorio de productos (tabla producto).
 * Todas las consultas usan PreparedStatement con parámetros (?), lo que
 * previene la inyección SQL incluso en el filtro de texto libre.
 */
public class ProductoRepositorioMySQL implements ProductoRepositorio {

    private static final String CONSULTA_BASE =
            "SELECT p.id_producto, p.nombre_producto, p.categoria, p.unidad_medida, p.stock_actual, "
            + "p.stock_minimo, p.precio_unitario, p.id_proveedor, pr.nombre AS nombre_proveedor, "
            + "p.activo, p.fecha_registro "
            + "FROM producto p LEFT JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor";

    private final ConexionMySQL conexion;

    public ProductoRepositorioMySQL(ConexionMySQL conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Producto> listar(FiltroProductos filtro) {
        StringBuilder sql = new StringBuilder(CONSULTA_BASE).append(" WHERE p.activo = 1");
        List<Object> parametros = new ArrayList<Object>();
        if (filtro.categoria != null) {
            sql.append(" AND p.categoria = ?");
            parametros.add(filtro.categoria);
        }
        if (filtro.texto != null) {
            sql.append(" AND p.nombre_producto LIKE ?");
            parametros.add("%" + filtro.texto + "%");
        }
        if (filtro.proveedorId != null) {
            sql.append(" AND p.id_proveedor = ?");
            parametros.add(filtro.proveedorId);
        }
        if (filtro.soloStockBajo) {
            sql.append(" AND p.stock_actual <= p.stock_minimo");
        }
        sql.append(" ORDER BY p.id_producto");

        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> lista = new ArrayList<Producto>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("listar productos", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Producto buscarPorId(long id) {
        return uno(CONSULTA_BASE + " WHERE p.id_producto = ?", id);
    }

    @Override
    public Producto buscarActivoPorNombre(String nombre) {
        return uno(CONSULTA_BASE + " WHERE p.activo = 1 AND p.nombre_producto = ?", nombre);
    }

    @Override
    public Producto guardar(Producto p) {
        String sql = "INSERT INTO producto (nombre_producto, categoria, unidad_medida, stock_actual, "
                + "stock_minimo, precio_unitario, id_proveedor, activo) VALUES (?, ?, ?, 0, ?, ?, ?, 1)";
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setString(3, p.getUnidadMedida());
            ps.setInt(4, p.getStockMinimo());
            ps.setBigDecimal(5, p.getPrecioUnitario());
            asignarProveedor(ps, 6, p.getProveedorId());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                return buscarPorId(llaves.getLong(1));
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("guardar el producto", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Producto actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre_producto = ?, categoria = ?, unidad_medida = ?, "
                + "stock_minimo = ?, precio_unitario = ?, id_proveedor = ? WHERE id_producto = ?";
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setString(3, p.getUnidadMedida());
            ps.setInt(4, p.getStockMinimo());
            ps.setBigDecimal(5, p.getPrecioUnitario());
            asignarProveedor(ps, 6, p.getProveedorId());
            ps.setLong(7, p.getId());
            ps.executeUpdate();
            return buscarPorId(p.getId());
        } catch (SQLException e) {
            throw ConexionMySQL.error("actualizar el producto", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public void desactivar(long id) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET activo = 0 WHERE id_producto = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ConexionMySQL.error("desactivar el producto", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static void asignarProveedor(PreparedStatement ps, int indice, Long proveedorId) throws SQLException {
        if (proveedorId == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setLong(indice, proveedorId);
        }
    }

    private Producto uno(String sql, Object parametro) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("consultar el producto", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id_producto"));
        p.setNombre(rs.getString("nombre_producto"));
        p.setCategoria(rs.getString("categoria"));
        p.setUnidadMedida(rs.getString("unidad_medida"));
        p.setStockActual(rs.getInt("stock_actual"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        long proveedorId = rs.getLong("id_proveedor");
        p.setProveedorId(rs.wasNull() ? null : proveedorId);
        p.setProveedorNombre(rs.getString("nombre_proveedor"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaRegistro(Fechas.formatear(rs.getTimestamp("fecha_registro")));
        return p;
    }
}
