package co.sena.raspicholados.api.http;

/**
 * Nivel de acceso exigido por una ruta de la API.
 *
 * PUBLICA        -> no requiere token (salud, documentación, login).
 * AUTENTICADA    -> requiere un token JWT válido de cualquier rol.
 * ADMINISTRADOR  -> requiere un token JWT con rol ADMINISTRADOR.
 */
public enum Seguridad {
    PUBLICA,
    AUTENTICADA,
    ADMINISTRADOR
}
