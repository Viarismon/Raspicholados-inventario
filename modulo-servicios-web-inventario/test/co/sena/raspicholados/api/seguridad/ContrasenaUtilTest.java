package co.sena.raspicholados.api.seguridad;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/** Pruebas del cifrado de contraseñas con PBKDF2 (mecanismo de seguridad). */
public class ContrasenaUtilTest {

    @Test
    public void elHashNoContieneLaContrasenaEnTextoPlano() {
        String hash = ContrasenaUtil.cifrar("Raspi2026*");
        assertTrue(hash.startsWith("pbkdf2_sha256$210000$"));
        assertFalse(hash.contains("Raspi2026*"));
    }

    @Test
    public void verificaLaContrasenaCorrecta() {
        String hash = ContrasenaUtil.cifrar("Raspi2026*");
        assertTrue(ContrasenaUtil.verificar("Raspi2026*", hash));
    }

    @Test
    public void rechazaUnaContrasenaIncorrecta() {
        String hash = ContrasenaUtil.cifrar("Raspi2026*");
        assertFalse(ContrasenaUtil.verificar("raspi2026*", hash));
    }

    @Test
    public void laMismaContrasenaGeneraHashesDistintosPorLaSal() {
        assertNotEquals(ContrasenaUtil.cifrar("Empleado2026"), ContrasenaUtil.cifrar("Empleado2026"));
    }

    @Test
    public void unHashMalFormadoNoSeAcepta() {
        assertFalse(ContrasenaUtil.verificar("Raspi2026*", "texto-cualquiera"));
    }
}
