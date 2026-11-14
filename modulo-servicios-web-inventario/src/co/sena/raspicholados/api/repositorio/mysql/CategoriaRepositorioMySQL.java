package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.repositorio.CategoriaRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC del repositorio de categorías (tabla categoria). */
public class CategoriaRepositorioMySQL implements CategoriaRepositorio {

    /** Incluye el conteo de productos activos de cada categoría. */
    private static final String CONSULTA_BASE =
            "SELECT c.id_categoria, c.nombre, c.descripcion, "
            + "(SELECT COUNT(*) FROM producto p WHERE p.categoria = c.nombre AND p.activo = 1) AS cantidad "
            + "FROM categoria c";

    private final ConexionMySQL conexion;

    public CategoriaRepositorioMySQL(ConexionMySQL conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Categoria> listar() {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(CONSULTA_BASE + " ORDER BY c.nombre");
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<Categoria>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionMySQL.error("listar categorías", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Categoria buscarPorId(long id) {
        return una(CONSULTA_BASE + " WHERE c.id_categoria = ?", id);
    }

    @Override
    public Categoria buscarPorNombre(String nombre) {
        return una(CONSULTA_BASE + " WHERE c.nombre = ?", nombre);
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                return buscarPorId(llaves.getLong(1));
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("guardar la categoría", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Categoria actualizar(Categoria categoria) {
        // La llave foránea producto.categoria -> categoria.nombre tiene
        // ON UPDATE CASCADE: MySQL actualiza los productos automáticamente.
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria = ?")) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setLong(3, categoria.getId());
            ps.executeUpdate();
            return buscarPorId(categoria.getId());
        } catch (SQLException e) {
            throw ConexionMySQL.error("actualizar la categoría", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public void eliminar(long id) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM categoria WHERE id_categoria = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ConexionMySQL.error("eliminar la categoría", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public int contarProductos(String nombreCategoria) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM producto WHERE categoria = ?")) {
            ps.setString(1, nombreCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("contar productos de la categoría", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private Categoria una(String sql, Object parametro) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("consultar la categoría", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static Categoria mapear(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria();
        cat.setId(rs.getLong("id_categoria"));
        cat.setNombre(rs.getString("nombre"));
        cat.setDescripcion(rs.getString("descripcion"));
        cat.setCantidadProductos(rs.getInt("cantidad"));
        return cat;
    }
}
