package co.sena.raspicholados.api.json;

/**
 * Excepción lanzada cuando un texto no cumple la sintaxis JSON.
 * Incluye la posición (índice de carácter) donde se detectó el error,
 * para que el cliente de la API pueda corregir su petición.
 */
public class JsonException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int posicion;

    public JsonException(String mensaje, int posicion) {
        super(mensaje + " (posición " + posicion + ")");
        this.posicion = posicion;
    }

    public int getPosicion() {
        return posicion;
    }
}
