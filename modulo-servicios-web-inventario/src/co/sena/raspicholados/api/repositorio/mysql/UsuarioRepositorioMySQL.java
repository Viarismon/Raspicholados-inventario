package co.sena.raspicholados.api.repositorio.mysql;

import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.repositorio.UsuarioRepositorio;
import co.sena.raspicholados.api.util.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC del repositorio de usuarios (tabla usuario). */
public class UsuarioRepositorioMySQL implements UsuarioRepositorio {

    private static final String COLUMNAS =
            "SELECT id_usuario, nombre_usuario, nombre_completo, rol, hash_contrasena, activo, fecha_registro FROM usuario";

    private final ConexionMySQL conexion;

    public UsuarioRepositorioMySQL(ConexionMySQL conexion) {
        this.conexion = conexion;
    }

    @Override
    public Usuario buscarPorId(long id) {
        return uno(COLUMNAS + " WHERE id_usuario = ?", id);
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        return uno(COLUMNAS + " WHERE nombre_usuario = ?", nombreUsuario);
    }

    @Override
    public List<Usuario> listar() {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(COLUMNAS + " ORDER BY id_usuario");
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> lista = new ArrayList<Usuario>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionMySQL.error("listar usuarios", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public long contar() {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ConexionMySQL.error("contar usuarios", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public Usuario guardar(Usuario u) {
        String sql = "INSERT INTO usuario (nombre_usuario, nombre_completo, rol, hash_contrasena, activo) VALUES (?, ?, ?, ?, ?)";
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombreUsuario());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getRol());
            ps.setString(4, u.getHashContrasena());
            ps.setBoolean(5, u.isActivo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                return buscarPorId(llaves.getLong(1));
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("guardar usuario", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    @Override
    public void actualizarContrasena(long id, String hashContrasena) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE usuario SET hash_contrasena = ? WHERE id_usuario = ?")) {
            ps.setString(1, hashContrasena);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ConexionMySQL.error("actualizar la contraseña", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private Usuario uno(String sql, Object parametro) {
        Connection c = conexion.obtener();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionMySQL.error("consultar usuario", e);
        } finally {
            ConexionMySQL.cerrar(c);
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id_usuario"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setRol(rs.getString("rol"));
        u.setHashContrasena(rs.getString("hash_contrasena"));
        u.setActivo(rs.getBoolean("activo"));
        u.setFechaRegistro(Fechas.formatear(rs.getTimestamp("fecha_registro")));
        return u;
    }
}
