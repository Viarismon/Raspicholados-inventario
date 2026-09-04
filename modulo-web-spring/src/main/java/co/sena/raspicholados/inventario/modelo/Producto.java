package co.sena.raspicholados.inventario.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Módulo: Inventario (versión web con framework)
 * Propósito: Entidad JPA que representa un producto del inventario.
 *            Es la misma entidad ya definida en las evidencias EV01
 *            (JDBC), EV02 (Servlets) y AA3-EV01 móvil (SQLite), ahora
 *            mapeada con anotaciones de Spring Data JPA para que el
 *            framework genere automáticamente las sentencias SQL.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-09-02
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre_producto", nullable = false, length = 80)
    private String nombreProducto;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "stock_minimo", nullable = false)
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

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
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
     * configurado (RF-04 del informe de especificaciones funcionales).
     */
    public boolean isTieneStockBajo() {
        return this.stockActual < this.stockMinimo;
    }
}
