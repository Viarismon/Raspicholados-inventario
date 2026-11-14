package co.sena.raspicholados.api.json;

import java.util.Map;

/**
 * Contrato para las clases del modelo que saben representarse como JSON.
 * El serializador {@link Json} invoca {@link #aMapa()} automáticamente.
 */
public interface ConvertibleJson {

    /** Devuelve las propiedades públicas del objeto en el orden deseado. */
    Map<String, Object> aMapa();
}
