package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.repositorio.UsuarioRepositorio;
import co.sena.raspicholados.api.seguridad.ContrasenaUtil;
import co.sena.raspicholados.api.seguridad.ControlIntentos;
import co.sena.raspicholados.api.seguridad.JwtUtil;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio de autenticación y gestión de usuarios.
 *
 * Reglas:
 *  - Contraseña: 8 a 64 caracteres, con al menos una letra y un número
 *    (corrige H-04: antes se aceptaban claves de 1 carácter).
 *  - 5 intentos fallidos bloquean la cuenta 15 minutos (corrige H-03).
 *  - El mensaje de error es el mismo si el usuario no existe o si la
 *    contraseña es incorrecta, para no revelar qué usuarios existen.
 *  - Solo un ADMINISTRADOR puede registrar usuarios nuevos.
 */
public class AutenticacionServicio {

    private static final String REGEX_USUARIO = "^[A-Za-z0-9._-]{3,30}$";
    /** Hash de referencia para igualar tiempos cuando el usuario no existe. */
    private static final String HASH_SENUELO = ContrasenaUtil.cifrar("senuelo-no-usado-1");

    private final UsuarioRepositorio usuarios;
    private final JwtUtil jwt;
    private final ControlIntentos intentos = new ControlIntentos();

    public AutenticacionServicio(UsuarioRepositorio usuarios, JwtUtil jwt) {
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    /** Crea el administrador inicial si la base de datos no tiene usuarios. */
    public boolean crearAdministradorInicial(String nombreUsuario, String contrasena) {
        if (usuarios.contar() > 0) {
            return false;
        }
        Usuario admin = new Usuario();
        admin.setNombreUsuario(nombreUsuario);
        admin.setNombreCompleto("Administrador Raspicholados");
        admin.setRol(Usuario.ROL_ADMINISTRADOR);
        admin.setHashContrasena(ContrasenaUtil.cifrar(contrasena));
        usuarios.guardar(admin);
        return true;
    }

    public Map<String, Object> iniciarSesion(Map<String, Object> datos) {
        Validador v = new Validador(datos);
        String nombreUsuario = v.texto("usuario", true, 1, 30);
        String contrasena = v.texto("contrasena", true, 1, 64);
        v.validar();

        long bloqueo = intentos.segundosDeBloqueo(nombreUsuario);
        if (bloqueo > 0) {
            throw cuentaBloqueada(bloqueo);
        }

        Usuario usuario = usuarios.buscarPorNombreUsuario(nombreUsuario);
        boolean valido;
        if (usuario == null) {
            ContrasenaUtil.verificar(contrasena, HASH_SENUELO); // mismo costo de tiempo
            valido = false;
        } else {
            valido = usuario.isActivo() && ContrasenaUtil.verificar(contrasena, usuario.getHashContrasena());
        }

        if (!valido) {
            int restantes = intentos.registrarFallo(nombreUsuario);
            if (restantes == 0) {
                throw cuentaBloqueada(ControlIntentos.MINUTOS_BLOQUEO * 60L);
            }
            throw new ApiException(401, "CREDENCIALES_INVALIDAS",
                    "Usuario o contraseña incorrectos. Intentos restantes antes del bloqueo: " + restantes);
        }

        intentos.registrarExito(nombreUsuario);
        Map<String, Object> respuesta = new LinkedHashMap<String, Object>();
        respuesta.put("token", jwt.generar(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol()));
        respuesta.put("tipo", "Bearer");
        respuesta.put("expiraEnSegundos", jwt.getMinutosVigencia() * 60L);
        respuesta.put("usuario", usuario);
        return respuesta;
    }

    public Usuario registrar(Map<String, Object> datos) {
        Validador v = new Validador(datos);
        String nombreUsuario = v.patron("nombreUsuario", true, 30, REGEX_USUARIO,
                "Use de 3 a 30 caracteres: letras, números, punto, guion o guion bajo");
        String nombreCompleto = v.texto("nombreCompleto", true, 3, 100);
        String contrasena = validarContrasena(v, "contrasena");
        String rol = v.opcion("rol", false, Usuario.ROL_ADMINISTRADOR, Usuario.ROL_EMPLEADO);
        v.validar();

        if (usuarios.buscarPorNombreUsuario(nombreUsuario) != null) {
            throw ApiException.conflicto("USUARIO_DUPLICADO",
                    "Ya existe un usuario con el nombre '" + nombreUsuario + "'");
        }
        Usuario nuevo = new Usuario();
        nuevo.setNombreUsuario(nombreUsuario);
        nuevo.setNombreCompleto(nombreCompleto);
        nuevo.setRol(rol == null ? Usuario.ROL_EMPLEADO : rol);
        nuevo.setHashContrasena(ContrasenaUtil.cifrar(contrasena));
        return usuarios.guardar(nuevo);
    }

    public void cambiarContrasena(UsuarioAutenticado autenticado, Map<String, Object> datos) {
        Validador v = new Validador(datos);
        String actual = v.texto("contrasenaActual", true, 1, 64);
        String nueva = validarContrasena(v, "contrasenaNueva");
        v.validar();

        Usuario usuario = usuarios.buscarPorId(autenticado.getId());
        if (usuario == null || !ContrasenaUtil.verificar(actual, usuario.getHashContrasena())) {
            throw new ApiException(401, "CREDENCIALES_INVALIDAS", "La contraseña actual no es correcta");
        }
        if (actual.equals(nueva)) {
            throw ApiException.solicitudInvalida("CONTRASENA_REPETIDA",
                    "La contraseña nueva debe ser diferente de la actual");
        }
        usuarios.actualizarContrasena(usuario.getId(), ContrasenaUtil.cifrar(nueva));
    }

    public Usuario perfil(UsuarioAutenticado autenticado) {
        Usuario usuario = usuarios.buscarPorId(autenticado.getId());
        if (usuario == null) {
            throw new ApiException(401, "TOKEN_INVALIDO", "El usuario del token ya no existe");
        }
        return usuario;
    }

    public List<Usuario> listar() {
        return usuarios.listar();
    }

    private static String validarContrasena(Validador v, String campo) {
        // Se valida sin recortar espacios internos.
        String contrasena = v.texto(campo, true, 8, 64);
        if (contrasena != null && (!contrasena.matches(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*")
                || !contrasena.matches(".*[0-9].*"))) {
            v.error(campo, "Debe contener al menos una letra y un número");
        }
        return contrasena;
    }

    private static ApiException cuentaBloqueada(long segundos) {
        return new ApiException(429, "CUENTA_BLOQUEADA",
                "Demasiados intentos fallidos. La cuenta está bloqueada temporalmente; intente de nuevo en "
                        + ((segundos + 59) / 60) + " minuto(s)")
                .conCabecera("Retry-After", String.valueOf(segundos));
    }
}
