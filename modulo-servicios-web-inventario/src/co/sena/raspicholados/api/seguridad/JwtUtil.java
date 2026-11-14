package co.sena.raspicholados.api.seguridad;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.json.Json;
import co.sena.raspicholados.api.json.JsonException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Emisión y verificación de tokens JWT (RFC 7519) firmados con HMAC-SHA256
 * (algoritmo HS256), implementados con las clases criptográficas del JDK.
 *
 * Resuelve la observación de diseño de la evidencia AA5-EV02: el login
 * anterior solo respondía "autenticación exitosa" sin emitir ninguna
 * credencial, por lo que el resto de servicios no tenía forma de saber
 * quién hacía cada petición. Ahora el login entrega un token con vigencia
 * limitada que el cliente envía en cada petición:
 *
 *     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * Contenido (payload) del token: sub (id), usuario, rol, iat, exp.
 */
public class JwtUtil {

    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODIFICADOR = Base64.getUrlDecoder();
    private static final String ENCABEZADO = CODIFICADOR.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] secreto;
    private final long minutosVigencia;

    public JwtUtil(String secreto, long minutosVigencia) {
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.minutosVigencia = minutosVigencia;
    }

    public long getMinutosVigencia() {
        return minutosVigencia;
    }

    /** Genera un token firmado para el usuario indicado. */
    public String generar(long idUsuario, String nombreUsuario, String rol) {
        long ahora = System.currentTimeMillis() / 1000L;
        Map<String, Object> carga = new LinkedHashMap<String, Object>();
        carga.put("sub", String.valueOf(idUsuario));
        carga.put("usuario", nombreUsuario);
        carga.put("rol", rol);
        carga.put("iat", ahora);
        carga.put("exp", ahora + minutosVigencia * 60L);
        String cargaCodificada = CODIFICADOR.encodeToString(
                Json.escribirCompacto(carga).getBytes(StandardCharsets.UTF_8));
        String contenido = ENCABEZADO + "." + cargaCodificada;
        return contenido + "." + firmar(contenido);
    }

    /**
     * Verifica la firma y la vigencia del token.
     *
     * @throws ApiException 401 TOKEN_INVALIDO o TOKEN_EXPIRADO
     */
    @SuppressWarnings("unchecked")
    public UsuarioAutenticado verificar(String token) {
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw invalido("El token no tiene el formato encabezado.carga.firma");
        }
        String firmaEsperada = firmar(partes[0] + "." + partes[1]);
        // Comparación en tiempo constante para no filtrar información por tiempos.
        if (!MessageDigest.isEqual(firmaEsperada.getBytes(StandardCharsets.UTF_8),
                partes[2].getBytes(StandardCharsets.UTF_8))) {
            throw invalido("La firma del token no es válida");
        }
        Map<String, Object> carga;
        try {
            String json = new String(DECODIFICADOR.decode(partes[1]), StandardCharsets.UTF_8);
            carga = (Map<String, Object>) Json.analizar(json);
        } catch (IllegalArgumentException | JsonException | ClassCastException e) {
            throw invalido("La carga del token no se pudo leer");
        }
        Object exp = carga.get("exp");
        long ahora = System.currentTimeMillis() / 1000L;
        if (!(exp instanceof BigDecimal) || ((BigDecimal) exp).longValue() < ahora) {
            throw new ApiException(401, "TOKEN_EXPIRADO",
                    "El token expiró. Inicie sesión de nuevo en POST /api/auth/login");
        }
        try {
            long id = Long.parseLong(String.valueOf(carga.get("sub")));
            return new UsuarioAutenticado(id, String.valueOf(carga.get("usuario")),
                    String.valueOf(carga.get("rol")));
        } catch (NumberFormatException e) {
            throw invalido("El token no identifica a un usuario válido");
        }
    }

    private String firmar(String contenido) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return CODIFICADOR.encodeToString(mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible firmar el token", e);
        }
    }

    private static ApiException invalido(String mensaje) {
        return new ApiException(401, "TOKEN_INVALIDO", mensaje);
    }
}
