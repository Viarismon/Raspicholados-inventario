package co.sena.raspicholados.inventario.modelo;

/**
 * Módulo: Inventario
 * Propósito: Clase modelo que representa un producto del inventario
 *            de la heladería RASPICHOLADOS (RF-01 del informe de
 *            especificaciones funcionales).
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String categoria;
    private String unidadMedida;
    private int stockActual;
    private int stockMinimo;

    public Producto() {
    }

    public Producto(String nombreProducto, String categoria, String unidadMedida,
                     int stockActual, int stockMinimo) {
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public Producto(int idProducto, String nombreProducto, String categoria, String unidadMedida,
                     int stockActual, int stockMinimo) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    /**
     * Indica si el producto está por debajo del stock mínimo
     * configurado (soporta RF-04: alertas de stock mínimo).
     */
    public boolean tieneStockBajo() {
        return this.stockActual < this.stockMinimo;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", unidadMedida='" + unidadMedida + '\'' +
                ", stockActual=" + stockActual +
                ", stockMinimo=" + stockMinimo +
                '}';
    }
}
