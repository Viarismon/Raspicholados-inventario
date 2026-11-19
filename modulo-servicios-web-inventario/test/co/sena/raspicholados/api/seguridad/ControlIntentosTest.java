package co.sena.raspicholados.api.seguridad;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Pruebas del bloqueo de cuentas tras intentos fallidos de inicio de sesión. */
public class ControlIntentosTest {

    @Test
    public void cuentaLosIntentosRestantes() {
        ControlIntentos c = new ControlIntentos();
        assertEquals(4, c.registrarFallo("admin"));
        assertEquals(3, c.registrarFallo("admin"));
    }

    @Test
    public void bloqueaLaCuentaAlQuintoFallo() {
        ControlIntentos c = new ControlIntentos();
        for (int i = 0; i < 4; i++) {
            c.registrarFallo("admin");
        }
        assertEquals(0, c.registrarFallo("admin"));
        assertTrue(c.segundosDeBloqueo("admin") > 14 * 60);
    }

    @Test
    public void elUsuarioNoDistingueMayusculas() {
        ControlIntentos c = new ControlIntentos();
        c.registrarFallo("Admin");
        assertEquals(3, c.registrarFallo("ADMIN"));
    }

    @Test
    public void unInicioExitosoReiniciaElConteo() {
        ControlIntentos c = new ControlIntentos();
        c.registrarFallo("laura");
        c.registrarFallo("laura");
        c.registrarExito("laura");
        assertEquals(4, c.registrarFallo("laura"));
        assertEquals(0, c.segundosDeBloqueo("laura"));
    }
}
