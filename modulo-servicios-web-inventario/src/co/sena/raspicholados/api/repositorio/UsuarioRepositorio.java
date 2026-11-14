package co.sena.raspicholados.api.repositorio;

import co.sena.raspicholados.api.modelo.Usuario;

import java.util.List;

/** Contrato de persistencia de usuarios (patrón Repositorio / DAO). */
public interface UsuarioRepositorio {

    Usuario buscarPorId(long id);

    /** Búsqueda sin distinguir mayúsculas de minúsculas. */
    Usuario buscarPorNombreUsuario(String nombreUsuario);

    List<Usuario> listar();

    long contar();

    Usuario guardar(Usuario usuario);

    void actualizarContrasena(long id, String hashContrasena);
}
