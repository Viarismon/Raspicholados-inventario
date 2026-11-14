package co.sena.raspicholados.api.seguridad;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos fallidos de inicio de sesión por usuario
 * (hallazgo H-03 de la AA5-EV02: no había límite y se podía probar
 * contraseñas sin restricción).
 *
 * Regla: después de MAXIMO_INTENTOS fallos seguidos, la cuenta queda
 * bloqueada durante MINUTOS_BLOQUEO. Un inicio de sesión exitoso reinicia
 * el contador. Se guarda en memoria: si el servidor se reinicia, los
 * contadores vuelven a cero (limitación documentada en el informe).
 */
public class ControlIntentos {

    public static final int MAXIMO_INTENTOS = 5;
    public static final int MINUTOS_BLOQUEO = 15;

    private static class Registro {
        int fallos;
        long bloqueadoHasta;
    }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<String, Registro>();

    /** Segundos que faltan para desbloquear la cuenta, o 0 si no está bloqueada. */
    public long segundosDeBloqueo(String usuario) {
        Registro r = registros.get(clave(usuario));
        if (r == null) {
            return 0;
        }
        synchronized (r) {
            long restante = r.bloqueadoHasta - System.currentTimeMillis();
            return restante > 0 ? (restante + 999) / 1000 : 0;
        }
    }

    /** Registra un fallo y devuelve los intentos que quedan antes del bloqueo. */
    public int registrarFallo(String usuario) {
        Registro nuevo = new Registro();
        Registro r = registros.putIfAbsent(clave(usuario), nuevo);
        if (r == null) {
            r = nuevo;
        }
        synchronized (r) {
            if (r.bloqueadoHasta > 0 && r.bloqueadoHasta < System.currentTimeMillis()) {
                r.fallos = 0;
                r.bloqueadoHasta = 0;
            }
            r.fallos++;
            if (r.fallos >= MAXIMO_INTENTOS) {
                r.bloqueadoHasta = System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000L;
                return 0;
            }
            return MAXIMO_INTENTOS - r.fallos;
        }
    }

    public void registrarExito(String usuario) {
        registros.remove(clave(usuario));
    }

    private static String clave(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase();
    }
}
