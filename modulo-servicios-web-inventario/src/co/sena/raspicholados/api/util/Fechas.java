package co.sena.raspicholados.api.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Formato único de fechas de la API: ISO 8601 local (yyyy-MM-dd'T'HH:mm:ss). */
public final class Fechas {

    private Fechas() {
    }

    public static String ahora() {
        return formatear(new Date());
    }

    public static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String formatear(Date fecha) {
        return fecha == null ? null : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(fecha);
    }

    public static String formatear(Timestamp fecha) {
        return fecha == null ? null : formatear(new Date(fecha.getTime()));
    }

    /** Valida el formato yyyy-MM-dd de forma estricta. */
    public static boolean esFechaValida(String texto) {
        if (texto == null || !texto.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        formato.setLenient(false);
        try {
            formato.parse(texto);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }
}
