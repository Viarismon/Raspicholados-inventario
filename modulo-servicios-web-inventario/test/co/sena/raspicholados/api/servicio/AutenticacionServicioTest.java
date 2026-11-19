package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.seguridad.JwtUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static co.sena.raspicholados.api.Datos.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pruebas del módulo de autenticación y usuarios. El cifrado PBKDF2 es
 * costoso a propósito, por eso el entorno se prepara una sola vez.
 */
public class AutenticacionServicioTest {

    private static JwtUtil jwt;
    private static AutenticacionServicio servicio;

    @BeforeClass
    public static void preparar() {
        EntornoMemoria e = new EntornoMemoria();
        jwt = new JwtUtil("secreto-de-pruebas-unitarias-raspicholados-2026", 120);
        servicio = new AutenticacionServicio(e.usuarios, jwt);
        assertTrue(servicio.crearAdministradorInicial("admin", "Raspi2026*"));
        servicio.registrar(json("{'nombreUsuario':'laura.gomez','nombreCompleto':'Laura Gómez',"
                + "'contrasena':'Empleado2026','rol':'EMPLEADO'}"));
    }

    @Test
    public void noCreaOtroAdministradorSiYaHayUsuarios() {
        assertFalse(servicio.crearAdministradorInicial("otro", "Otra2026*"));
    }

    @Test
    public void unLoginCorrectoEntregaUnTokenValido() {
        Map<String, Object> r = servicio.iniciarSesion(json("{'usuario':'admin','contrasena':'Raspi2026*'}"));
        assertEquals("Bearer", r.get("tipo"));
        assertNotNull(r.get("token"));
        assertTrue(jwt.verificar((String) r.get("token")).esAdministrador());
    }

    @Test
    public void unaContrasenaIncorrectaResponde401() {
        try {
            servicio.iniciarSesion(json("{'usuario':'laura.gomez','contrasena':'otraClave99'}"));
            fail("Debía responder 401");
        } catch (ApiException ex) {
            assertEquals(401, ex.getEstado());
            assertEquals("CREDENCIALES_INVALIDAS", ex.getCodigo());
        }
    }

    @Test
    public void unUsuarioInexistenteResponde401SinRevelarloSiExiste() {
        try {
            servicio.iniciarSesion(json("{'usuario':'nadie','contrasena':'Cualquiera1'}"));
            fail("Debía responder 401");
        } catch (ApiException ex) {
            assertEquals("CREDENCIALES_INVALIDAS", ex.getCodigo());
        }
    }

    @Test
    public void elEmpleadoRegistradoTieneRolEmpleado() {
        Map<String, Object> r = servicio.iniciarSesion(json("{'usuario':'laura.gomez','contrasena':'Empleado2026'}"));
        assertEquals(Usuario.ROL_EMPLEADO, ((Usuario) r.get("usuario")).getRol());
    }

    @Test
    public void rechazaContrasenasDebilesYUsuariosDuplicados() {
        try {
            servicio.registrar(json("{'nombreUsuario':'pedro','nombreCompleto':'Pedro Ruiz','contrasena':'abcdefgh'}"));
            fail("Debía rechazar la contraseña sin números");
        } catch (ApiException ex) {
            assertEquals(400, ex.getEstado());
        }
        try {
            servicio.registrar(json("{'nombreUsuario':'laura.gomez','nombreCompleto':'Otra Laura','contrasena':'Clave2026'}"));
            fail("Debía detectar el usuario duplicado");
        } catch (ApiException ex) {
            assertEquals(409, ex.getEstado());
        }
    }
}
