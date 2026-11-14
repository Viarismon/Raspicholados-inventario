package co.sena.raspicholados.api.controlador;

import co.sena.raspicholados.api.http.Respuesta;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.http.Seguridad;
import co.sena.raspicholados.api.http.Solicitud;
import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.servicio.AutenticacionServicio;

/**
 * Servicios de autenticación y usuarios.
 *
 *   POST /api/auth/login          (pública)        -> token JWT
 *   GET  /api/auth/perfil         (autenticada)    -> datos del usuario del token
 *   PUT  /api/auth/contrasena     (autenticada)    -> cambiar la propia contraseña
 *   POST /api/usuarios            (administrador)  -> registrar usuario
 *   GET  /api/usuarios            (administrador)  -> listar usuarios
 */
public class AutenticacionControlador {

    private final AutenticacionServicio servicio;

    public AutenticacionControlador(AutenticacionServicio servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.post("/api/auth/login", Seguridad.PUBLICA, this::login);
        router.get("/api/auth/perfil", Seguridad.AUTENTICADA, this::perfil);
        router.put("/api/auth/contrasena", Seguridad.AUTENTICADA, this::cambiarContrasena);
        router.post("/api/usuarios", Seguridad.ADMINISTRADOR, this::registrarUsuario);
        router.get("/api/usuarios", Seguridad.ADMINISTRADOR, this::listarUsuarios);
    }

    private Respuesta login(Solicitud s) throws Exception {
        return Respuesta.okConMensaje("Autenticación exitosa", servicio.iniciarSesion(s.cuerpoJson()));
    }

    private Respuesta perfil(Solicitud s) {
        return Respuesta.ok(servicio.perfil(s.getUsuario()));
    }

    private Respuesta cambiarContrasena(Solicitud s) throws Exception {
        servicio.cambiarContrasena(s.getUsuario(), s.cuerpoJson());
        return Respuesta.okConMensaje("Contraseña actualizada correctamente", null);
    }

    private Respuesta registrarUsuario(Solicitud s) throws Exception {
        Usuario u = servicio.registrar(s.cuerpoJson());
        return Respuesta.creado("Usuario registrado correctamente", u, "/api/usuarios/" + u.getId());
    }

    private Respuesta listarUsuarios(Solicitud s) {
        return Respuesta.ok(servicio.listar());
    }
}
