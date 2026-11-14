package co.sena.raspicholados.api.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Almacenamiento seguro de contraseñas con PBKDF2-HMAC-SHA256.
 *
 * Mejora frente a la AA5-EV01, que usaba un solo SHA-256 con salt: PBKDF2
 * aplica la función hash miles de veces (factor de trabajo), lo que hace
 * mucho más costoso un ataque de fuerza bruta si la base de datos se filtra.
 * Es el algoritmo recomendado por OWASP cuando se requiere conformidad
 * FIPS-140 y está disponible en el JDK sin librerías externas. Para un
 * despliegue productivo se podría migrar a Argon2id.
 *
 * Formato almacenado (una sola columna):
 *     pbkdf2_sha256$iteraciones$saltBase64$hashBase64
 */
public final class ContrasenaUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ContrasenaUtil() {
    }

    /** Genera el texto a guardar en la base de datos para una contraseña nueva. */
    public static String cifrar(String contrasena) {
        byte[] salt = new byte[16];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(contrasena.toCharArray(), salt, ITERACIONES);
        return "pbkdf2_sha256$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Comprueba si la contraseña ingresada corresponde al hash almacenado. */
    public static boolean verificar(String contrasena, String almacenado) {
        if (contrasena == null || almacenado == null) {
            return false;
        }
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !"pbkdf2_sha256".equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(contrasena.toCharArray(), salt, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] contrasena, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(contrasena, salt, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible derivar la clave", e);
        }
    }
}
