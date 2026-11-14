package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Valida los datos de entrada de un cuerpo JSON y ACUMULA los errores, de
 * modo que el cliente recibe todos los problemas en una sola respuesta
 * (y no uno por uno). Al final, {@link #validar()} lanza un 400
 * VALIDACION_FALLIDA con la lista de errores por campo:
 *
 * <pre>
 * "detalles": [
 *   { "campo": "nombre", "mensaje": "Es obligatorio" },
 *   { "campo": "precioUnitario", "mensaje": "Debe ser mayor o igual a 0" }
 * ]
 * </pre>
 */
public class Validador {

    private final Map<String, Object> datos;
    private final List<Map<String, Object>> errores = new ArrayList<Map<String, Object>>();

    public Validador(Map<String, Object> datos) {
        this.datos = datos;
    }

    public boolean contiene(String campo) {
        return datos.containsKey(campo) && datos.get(campo) != null;
    }

    /** Texto con longitud mínima y máxima; se recortan los espacios de los extremos. */
    public String texto(String campo, boolean requerido, int minimo, int maximo) {
        Object valor = datos.get(campo);
        if (valor == null) {
            if (requerido) {
                error(campo, "Es obligatorio");
            }
            return null;
        }
        if (!(valor instanceof String)) {
            error(campo, "Debe ser un texto");
            return null;
        }
        String texto = ((String) valor).trim();
        if (texto.isEmpty()) {
            if (requerido) {
                error(campo, "No puede estar vacío");
            }
            return null;
        }
        if (texto.length() < minimo) {
            error(campo, "Debe tener al menos " + minimo + " caracteres");
        } else if (texto.length() > maximo) {
            error(campo, "Debe tener máximo " + maximo + " caracteres");
        }
        return texto;
    }

    /** Texto que además debe cumplir una expresión regular. */
    public String patron(String campo, boolean requerido, int maximo, String regex, String mensaje) {
        String texto = texto(campo, requerido, 1, maximo);
        if (texto != null && !texto.matches(regex)) {
            error(campo, mensaje);
        }
        return texto;
    }

    /** Número entero dentro de un rango. Rechaza decimales como 2.5. */
    public Integer entero(String campo, boolean requerido, int minimo, int maximo) {
        Object valor = datos.get(campo);
        if (valor == null) {
            if (requerido) {
                error(campo, "Es obligatorio");
            }
            return null;
        }
        if (!(valor instanceof BigDecimal)) {
            error(campo, "Debe ser un número entero (sin comillas)");
            return null;
        }
        BigDecimal numero = (BigDecimal) valor;
        if (numero.signum() != 0 && numero.stripTrailingZeros().scale() > 0) {
            error(campo, "Debe ser un número entero, sin decimales");
            return null;
        }
        if (numero.compareTo(BigDecimal.valueOf(minimo)) < 0) {
            error(campo, "Debe ser mayor o igual a " + minimo);
            return null;
        }
        if (numero.compareTo(BigDecimal.valueOf(maximo)) > 0) {
            error(campo, "Debe ser menor o igual a " + maximo);
            return null;
        }
        return numero.intValue();
    }

    /** Identificador (entero positivo). */
    public Long id(String campo, boolean requerido) {
        Integer valor = entero(campo, requerido, 1, Integer.MAX_VALUE);
        return valor == null ? null : Long.valueOf(valor);
    }

    /** Valor monetario no negativo con máximo dos decimales. */
    public BigDecimal dinero(String campo, boolean requerido) {
        Object valor = datos.get(campo);
        if (valor == null) {
            if (requerido) {
                error(campo, "Es obligatorio");
            }
            return null;
        }
        if (!(valor instanceof BigDecimal)) {
            error(campo, "Debe ser un número (sin comillas)");
            return null;
        }
        BigDecimal numero = (BigDecimal) valor;
        if (numero.signum() < 0) {
            error(campo, "Debe ser mayor o igual a 0");
            return null;
        }
        if (numero.stripTrailingZeros().scale() > 2) {
            error(campo, "Admite máximo 2 decimales");
            return null;
        }
        if (numero.compareTo(new BigDecimal("9999999999.99")) > 0) {
            error(campo, "Valor demasiado alto");
            return null;
        }
        return numero.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** Uno de los valores permitidos (sin distinguir mayúsculas). Devuelve en mayúsculas. */
    public String opcion(String campo, boolean requerido, String... permitidas) {
        String texto = texto(campo, requerido, 1, 30);
        if (texto == null) {
            return null;
        }
        for (String permitida : permitidas) {
            if (permitida.equalsIgnoreCase(texto)) {
                return permitida;
            }
        }
        error(campo, "Valor no permitido. Use uno de: " + String.join(", ", permitidas));
        return null;
    }

    public void error(String campo, String mensaje) {
        Map<String, Object> e = new LinkedHashMap<String, Object>();
        e.put("campo", campo);
        e.put("mensaje", mensaje);
        errores.add(e);
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    /** Lanza 400 VALIDACION_FALLIDA si se acumuló al menos un error. */
    public void validar() {
        if (!errores.isEmpty()) {
            throw new ApiException(400, "VALIDACION_FALLIDA",
                    "Los datos enviados no son válidos (" + errores.size() + " error(es))", errores);
        }
    }
}
