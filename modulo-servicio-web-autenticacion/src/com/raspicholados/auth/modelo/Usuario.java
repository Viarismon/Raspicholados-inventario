package com.raspicholados.auth.modelo;

/**
 * Modelo de dominio que representa un usuario del sistema Raspicholados.
 *
 * Nunca se almacena la contraseña en texto plano: se guarda el hash
 * (resultado de aplicar SHA-256 + "salt" aleatoria) junto con la salt
 * usada, siguiendo las recomendaciones de gestión de credenciales del
 * NIST Special Publication 800-63B (2020) para autenticación digital.
 */
public class Usuario {

    private final String nombreUsuario;
    private final String hashContrasena; // Hash SHA-256 de (salt + contraseña)
    private final String salt;           // Valor aleatorio único por usuario

    public Usuario(String nombreUsuario, String hashContrasena, String salt) {
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.salt = salt;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public String getSalt() {
        return salt;
    }
}
