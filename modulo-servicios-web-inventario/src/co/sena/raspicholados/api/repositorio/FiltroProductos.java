package co.sena.raspicholados.api.repositorio;

/** Criterios opcionales de búsqueda de productos (null = sin filtro). */
public class FiltroProductos {

    public String categoria;
    public String texto;
    public Long proveedorId;
    /** Si es true, solo productos en estado BAJO o AGOTADO. */
    public boolean soloStockBajo;
}
