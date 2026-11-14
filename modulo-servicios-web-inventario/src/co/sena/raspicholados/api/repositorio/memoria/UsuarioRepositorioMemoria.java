package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.repositorio.UsuarioRepositorio;
import co.sena.raspicholados.api.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del repositorio de usuarios. */
public class UsuarioRepositorioMemoria implements UsuarioRepositorio {

    private final BaseDatosMemoria bd;

    public UsuarioRepositorioMemoria(BaseDatosMemoria bd) {
        this.bd = bd;
    }

    @Override
    public Usuario buscarPorId(long id) {
        synchronized (bd.CANDADO) {
            return bd.usuarios.get(id);
        }
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        synchronized (bd.CANDADO) {
            for (Usuario u : bd.usuarios.values()) {
                if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                    return u;
                }
            }
            return null;
        }
    }

    @Override
    public List<Usuario> listar() {
        synchronized (bd.CANDADO) {
            return new ArrayList<Usuario>(bd.usuarios.values());
        }
    }

    @Override
    public long contar() {
        synchronized (bd.CANDADO) {
            return bd.usuarios.size();
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        synchronized (bd.CANDADO) {
            usuario.setId(++bd.secuenciaUsuario);
            usuario.setFechaRegistro(Fechas.ahora());
            bd.usuarios.put(usuario.getId(), usuario);
            return usuario;
        }
    }

    @Override
    public void actualizarContrasena(long id, String hashContrasena) {
        synchronized (bd.CANDADO) {
            Usuario u = bd.usuarios.get(id);
            if (u != null) {
                u.setHashContrasena(hashContrasena);
            }
        }
    }
}
