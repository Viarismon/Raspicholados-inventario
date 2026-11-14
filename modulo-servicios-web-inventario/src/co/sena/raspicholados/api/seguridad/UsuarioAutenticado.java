package co.sena.raspicholados.api.seguridad;

/**
 * Datos del usuario extraídos de un token JWT válido. Es lo único que los
 * controladores conocen del usuario que hace la petición.
 */
public class UsuarioAutenticado {

    private final long id;
    private final String nombreUsuario;
    private final String rol;

    public UsuarioAutenticado(long id, String nombreUsuario, String rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    public long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public boolean esAdministrador() {
        return "ADMINISTRADOR".equals(rol);
    }
}
