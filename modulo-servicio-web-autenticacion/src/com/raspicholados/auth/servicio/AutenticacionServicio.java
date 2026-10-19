package com.raspicholados.auth.servicio;

import com.raspicholados.auth.modelo.Usuario;
import com.raspicholados.auth.repositorio.UsuarioRepositorio;
import com.raspicholados.auth.util.HashUtil;

/**
 * Capa de servicio (lógica de negocio) para el registro y la
 * autenticación de usuarios. Se mantiene separada de los controladores
 * web (paquete "web") siguiendo el patrón de arquitectura en capas
 * (presentación - negocio - persistencia), coherente con el resto del
 * proyecto Raspicholados.
 */
public class AutenticacionServicio {

    private final UsuarioRepositorio repositorio;

    public AutenticacionServicio(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Resultado posible de una operación de registro o autenticación.
     */
    public enum Resultado {
        EXITO,
        USUARIO_YA_EXISTE,
        CREDENCIALES_INVALIDAS,
        DATOS_INCOMPLETOS
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param nombreUsuario nombre de usuario (no puede estar vacío)
     * @param contrasena    contraseña en texto plano (se almacena su hash)
     * @return EXITO si se registró correctamente; USUARIO_YA_EXISTE si el
     *         nombre ya está en uso; DATOS_INCOMPLETOS si faltan campos.
     */
    public Resultado registrar(String nombreUsuario, String contrasena) {
        if (esVacio(nombreUsuario) || esVacio(contrasena)) {
            return Resultado.DATOS_INCOMPLETOS;
        }
        if (repositorio.existe(nombreUsuario)) {
            return Resultado.USUARIO_YA_EXISTE;
        }

        String salt = HashUtil.generarSalt();
        String hash = HashUtil.calcularHash(contrasena, salt);
        repositorio.guardar(new Usuario(nombreUsuario, hash, salt));

        return Resultado.EXITO;
    }

    /**
     * Valida las credenciales de un usuario contra las almacenadas.
     *
     * @param nombreUsuario nombre de usuario ingresado
     * @param contrasena    contraseña en texto plano ingresada
     * @return EXITO si la autenticación es correcta; CREDENCIALES_INVALIDAS
     *         si el usuario no existe o la contraseña no coincide;
     *         DATOS_INCOMPLETOS si faltan campos.
     */
    public Resultado autenticar(String nombreUsuario, String contrasena) {
        if (esVacio(nombreUsuario) || esVacio(contrasena)) {
            return Resultado.DATOS_INCOMPLETOS;
        }

        Usuario usuario = repositorio.buscarPorNombre(nombreUsuario);
        if (usuario == null) {
            return Resultado.CREDENCIALES_INVALIDAS;
        }

        String hashCalculado = HashUtil.calcularHash(contrasena, usuario.getSalt());
        boolean coincide = hashCalculado.equals(usuario.getHashContrasena());

        return coincide ? Resultado.EXITO : Resultado.CREDENCIALES_INVALIDAS;
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
