package co.sena.raspicholados.api.repositorio;

/**
 * Regla de negocio que calcula el nuevo stock a partir del actual.
 * La define el servicio (ENTRADA, SALIDA o AJUSTE) y la ejecuta el
 * repositorio dentro de la transacción, con el producto bloqueado.
 */
public interface OperacionStock {

    int calcularNuevoStock(int stockActual);
}
