package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.modelo.Proveedor;
import co.sena.raspicholados.api.repositorio.ProveedorRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC del repositorio de proveedores (tabla proveedor). */
public class ProveedorRepositorioMySQL implements ProveedorRepositorio {

    private static final String COLUMNAS =
            "SELECT id_proveedor, nit, nombre, telefono, correo, direccion FROM proveedor";

    private final ConexionMySQL conexion;

    public ProveedorRepositorioMySQL(ConexionMySQL conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Proveedor> listar() {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(COLUMNAS + " ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            List<Proveedor> lista = new ArrayList<Proveedor>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionMySQL.error("listar proveedores", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Proveedor buscarPorId(long id) {
        return uno(COLUMNAS + " WHERE id_proveedor = ?", id);
    }

    @Override
    public Proveedor buscarPorNit(String nit) {
        return uno(COLUMNAS + " WHERE nit = ?", nit);
    }

    @Override
    public Proveedor guardar(Proveedor p) {
        String sql = "INSERT INTO proveedor (nit, nombre, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, p);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                return buscarPorId(llaves.getLong(1));
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("guardar el proveedor", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Proveedor actualizar(Proveedor p) {
        String sql = "UPDATE proveedor SET nit = ?, nombre = ?, telefono = ?, correo = ?, direccion = ? WHERE id_proveedor = ?";
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setLong(6, p.getId());
            ps.executeUpdate();
            return buscarPorId(p.getId());
        } catch (SQLException e) {
            throw ConexionMySQL.error("actualizar el proveedor", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public void eliminar(long id) {
        // La llave foránea de producto tiene ON DELETE SET NULL.
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM proveedor WHERE id_proveedor = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ConexionMySQL.error("eliminar el proveedor", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public int contarProductosActivos(long proveedorId) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM producto WHERE id_proveedor = ? AND activo = 1")) {
            ps.setLong(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("contar productos del proveedor", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static void asignar(PreparedStatement ps, Proveedor p) throws SQLException {
        ps.setString(1, p.getNit());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getTelefono());
        ps.setString(4, p.getCorreo());
        ps.setString(5, p.getDireccion());
    }

    private Proveedor uno(String sql, Object parametro) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("consultar el proveedor", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("id_proveedor"));
        p.setNit(rs.getString("nit"));
        p.setNombre(rs.getString("nombre"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        return p;
    }
}
