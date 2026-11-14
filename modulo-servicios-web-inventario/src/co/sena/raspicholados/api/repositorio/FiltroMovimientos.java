package co.sena.raspicholados.api.repositorio;

/** Criterios opcionales de búsqueda de movimientos (null = sin filtro). */
public class FiltroMovimientos {

    public Long productoId;
    public String tipo;
    /** Fecha inicial inclusiva, formato yyyy-MM-dd. */
    public String desde;
    /** Fecha final inclusiva, formato yyyy-MM-dd. */
    public String hasta;
    /** Máximo de registros a devolver (los más recientes primero). */
    public int limite = 100;
}
