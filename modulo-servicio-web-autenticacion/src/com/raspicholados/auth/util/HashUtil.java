package com.raspicholados.auth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad encargada de generar "salts" aleatorias y de calcular el hash
 * de las contraseñas. Se utiliza SHA-256 combinado con una salt única por
 * usuario para mitigar ataques de diccionario y de tablas precalculadas
 * (rainbow tables), en línea con lo señalado por Stallings y Brown (2018)
 * sobre el manejo seguro de credenciales en sistemas de información.
 *
 * Nota académica: para un entorno productivo real se recomendaría un
 * algoritmo de derivación de clave más costoso computacionalmente, como
 * bcrypt, scrypt o Argon2 (OWASP, 2021), en lugar de SHA-256 simple. Se
 * usa SHA-256 aquí por ser nativo del JDK y suficiente para fines
 * formativos de esta evidencia.
 */
public class HashUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Genera una salt aleatoria de 16 bytes codificada en Base64. */
    public static String generarSalt() {
        byte[] bytesSalt = new byte[16];
        RANDOM.nextBytes(bytesSalt);
        return Base64.getEncoder().encodeToString(bytesSalt);
    }

    /**
     * Calcula el hash SHA-256 de la combinación (salt + contraseña).
     *
     * @param contrasena contraseña en texto plano ingresada por el usuario
     * @param salt       salt asociada al usuario
     * @return hash resultante codificado en Base64
     */
    public static String calcularHash(String contrasena, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes("UTF-8"));
            byte[] bytesHash = digest.digest(contrasena.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(bytesHash);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // No debería ocurrir: SHA-256 y UTF-8 están garantizados en el JDK.
            throw new RuntimeException("Error al calcular el hash de la contraseña", e);
        }
    }
}
