package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.repositorio.FiltroMovimientos;
import co.sena.raspicholados.api.repositorio.MovimientoRepositorio;
import co.sena.raspicholados.api.repositorio.OperacionStock;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;
import co.sena.raspicholados.api.util.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del repositorio de movimientos (tabla
 * movimiento_inventario). El registro de un movimiento se hace dentro de
 * una transacción con bloqueo de fila (SELECT ... FOR UPDATE), de modo que
 * la lectura del stock, la validación y la actualización ocurren como una
 * sola unidad indivisible (propiedades ACID).
 */
public class MovimientoRepositorioMySQL implements MovimientoRepositorio {

    private static final String CONSULTA_BASE =
            "SELECT m.id_movimiento, m.id_producto, p.nombre_producto, m.tipo, m.cantidad, "
            + "m.stock_anterior, m.stock_resultante, m.motivo, m.id_usuario, u.nombre_usuario, m.fecha "
            + "FROM movimiento_inventario m "
            + "JOIN producto p ON p.id_producto = m.id_producto "
            + "JOIN usuario u ON u.id_usuario = m.id_usuario";

    private final ConexionMySQL conexion;

    public MovimientoRepositorioMySQL(ConexionMySQL conexion) {
        this.conexion = conexion;
    }

    @Override
    public Movimiento registrar(long productoId, String tipo, int cantidad, String motivo,
                                UsuarioAutenticado usuario, OperacionStock operacion) {
        Connection c = conexion.obtener();
        boolean confirmado = false;
        try {
            c.setAutoCommit(false);
            int anterior;
            try (PreparedStatement bloqueo = c.prepareStatement(
                    "SELECT stock_actual FROM producto WHERE id_producto = ? FOR UPDATE")) {
                bloqueo.setLong(1, productoId);
                try (ResultSet rs = bloqueo.executeQuery()) {
                    rs.next();
                    anterior = rs.getInt(1);
                }
            }
            int nuevo = operacion.calcularNuevoStock(anterior); // puede lanzar ApiException -> rollback

            try (PreparedStatement actualizar = c.prepareStatement(
                    "UPDATE producto SET stock_actual = ? WHERE id_producto = ?")) {
                actualizar.setInt(1, nuevo);
                actualizar.setLong(2, productoId);
                actualizar.executeUpdate();
            }
            long idMovimiento;
            try (PreparedStatement insertar = c.prepareStatement(
                    "INSERT INTO movimiento_inventario (id_producto, tipo, cantidad, stock_anterior, "
                    + "stock_resultante, motivo, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertar.setLong(1, productoId);
                insertar.setString(2, tipo);
                insertar.setInt(3, cantidad);
                insertar.setInt(4, anterior);
                insertar.setInt(5, nuevo);
                insertar.setString(6, motivo);
                insertar.setLong(7, usuario.getId());
                insertar.executeUpdate();
                try (ResultSet llaves = insertar.getGeneratedKeys()) {
                    llaves.next();
                    idMovimiento = llaves.getLong(1);
                }
            }
            c.commit();
            confirmado = true;
            return buscarPorId(idMovimiento);
        } catch (SQLException e) {
            throw ConexionMySQL.error("registrar el movimiento", e);
        } finally {
            if (!confirmado) {
                try {
                    c.rollback();
                } catch (SQLException ignorada) {
                    // Se conserva la excepción original.
                }
            }
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public List<Movimiento> listar(FiltroMovimientos filtro) {
        StringBuilder sql = new StringBuilder(CONSULTA_BASE).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<Object>();
        if (filtro.productoId != null) {
            sql.append(" AND m.id_producto = ?");
            parametros.add(filtro.productoId);
        }
        if (filtro.tipo != null) {
            sql.append(" AND m.tipo = ?");
            parametros.add(filtro.tipo);
        }
        if (filtro.desde != null) {
            sql.append(" AND m.fecha >= ?");
            parametros.add(filtro.desde + " 00:00:00");
        }
        if (filtro.hasta != null) {
            sql.append(" AND m.fecha <= ?");
            parametros.add(filtro.hasta + " 23:59:59");
        }
        sql.append(" ORDER BY m.id_movimiento DESC LIMIT ?");
        parametros.add(filtro.limite);

        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Movimiento> lista = new ArrayList<Movimiento>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("listar movimientos", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Movimiento buscarPorId(long id) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(CONSULTA_BASE + " WHERE m.id_movimiento = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("consultar el movimiento", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static Movimiento mapear(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setId(rs.getLong("id_movimiento"));
        m.setProductoId(rs.getLong("id_producto"));
        m.setProductoNombre(rs.getString("nombre_producto"));
        m.setTipo(rs.getString("tipo"));
        m.setCantidad(rs.getInt("cantidad"));
        m.setStockAnterior(rs.getInt("stock_anterior"));
        m.setStockResultante(rs.getInt("stock_resultante"));
        m.setMotivo(rs.getString("motivo"));
        m.setUsuarioId(rs.getLong("id_usuario"));
        m.setUsuarioNombre(rs.getString("nombre_usuario"));
        m.setFecha(Fechas.formatear(rs.getTimestamp("fecha")));
        return m;
    }
}
