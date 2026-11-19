package co.sena.raspicholados.api.seguridad;

import co.sena.raspicholados.api.http.ApiException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Pruebas de la generación y verificación de tokens JWT (HS256). */
public class JwtUtilTest {

    private static final String SECRETO = "secreto-de-pruebas-unitarias-raspicholados-2026";

    @Test
    public void unTokenGeneradoSeVerificaYConservaLosDatos() {
        JwtUtil jwt = new JwtUtil(SECRETO, 120);
        UsuarioAutenticado u = jwt.verificar(jwt.generar(7L, "laura", "EMPLEADO"));
        assertEquals(7L, u.getId());
        assertEquals("laura", u.getNombreUsuario());
        assertFalse(u.esAdministrador());
    }

    @Test
    public void elTokenTieneTresPartes() {
        String token = new JwtUtil(SECRETO, 120).generar(1L, "admin", "ADMINISTRADOR");
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    public void unTokenFirmadoConOtroSecretoSeRechaza() {
        String token = new JwtUtil("otro-secreto-distinto-de-al-menos-32-caracteres", 120).generar(1L, "admin", "ADMINISTRADOR");
        esperar401(new JwtUtil(SECRETO, 120), token, "TOKEN_INVALIDO");
    }

    @Test
    public void unTokenAlteradoSeRechaza() {
        JwtUtil jwt = new JwtUtil(SECRETO, 120);
        String token = jwt.generar(2L, "laura", "EMPLEADO");
        String[] partes = token.split("\\.");
        // Se intenta cambiar el rol reemplazando la carga útil por la de otro token.
        String otraCarga = jwt.generar(2L, "laura", "ADMINISTRADOR").split("\\.")[1];
        esperar401(jwt, partes[0] + "." + otraCarga + "." + partes[2], "TOKEN_INVALIDO");
    }

    @Test
    public void unTokenVencidoSeRechaza() {
        JwtUtil vencido = new JwtUtil(SECRETO, -1);
        esperar401(vencido, vencido.generar(1L, "admin", "ADMINISTRADOR"), "TOKEN_EXPIRADO");
    }

    @Test
    public void elRolAdministradorSeReconoce() {
        JwtUtil jwt = new JwtUtil(SECRETO, 120);
        assertTrue(jwt.verificar(jwt.generar(1L, "admin", "ADMINISTRADOR")).esAdministrador());
    }

    private static void esperar401(JwtUtil jwt, String token, String codigo) {
        try {
            jwt.verificar(token);
            fail("Debía rechazar el token");
        } catch (ApiException e) {
            assertEquals(401, e.getEstado());
            assertEquals(codigo, e.getCodigo());
        }
    }
}
