package co.sena.raspicholados.api.modelo;

import co.sena.raspicholados.api.json.ConvertibleJson;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Producto del inventario. Conserva las columnas originales de la tabla
 * producto (AA2-EV01) y agrega precio, proveedor y estado activo.
 *
 * El campo estadoStock es calculado (no se guarda):
 *   AGOTADO -> stockActual == 0
 *   BAJO    -> stockActual &lt;= stockMinimo
 *   NORMAL  -> en otro caso
 */
public class Producto implements ConvertibleJson {

    public static final String ESTADO_AGOTADO = "AGOTADO";
    public static final String ESTADO_BAJO = "BAJO";
    public static final String ESTADO_NORMAL = "NORMAL";

    private long id;
    private String nombre;
    private String categoria;
    private String unidadMedida;
    private int stockActual;
    private int stockMinimo;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private Long proveedorId;
    private String proveedorNombre;
    private boolean activo = true;
    private String fechaRegistro;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstadoStock() {
        if (stockActual <= 0) {
            return ESTADO_AGOTADO;
        }
        if (stockActual <= stockMinimo) {
            return ESTADO_BAJO;
        }
        return ESTADO_NORMAL;
    }

    /** Valor del inventario de este producto: stock x precio unitario. */
    public BigDecimal getValorInventario() {
        return precioUnitario.multiply(BigDecimal.valueOf(stockActual));
    }

    public Producto copia() {
        Producto p = new Producto();
        p.id = id;
        p.nombre = nombre;
        p.categoria = categoria;
        p.unidadMedida = unidadMedida;
        p.stockActual = stockActual;
        p.stockMinimo = stockMinimo;
        p.precioUnitario = precioUnitario;
        p.proveedorId = proveedorId;
        p.proveedorNombre = proveedorNombre;
        p.activo = activo;
        p.fechaRegistro = fechaRegistro;
        return p;
    }

    @Override
    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("nombre", nombre);
        m.put("categoria", categoria);
        m.put("unidadMedida", unidadMedida);
        m.put("stockActual", stockActual);
        m.put("stockMinimo", stockMinimo);
        m.put("estadoStock", getEstadoStock());
        m.put("precioUnitario", precioUnitario);
        m.put("valorInventario", getValorInventario());
        m.put("proveedorId", proveedorId);
        m.put("proveedorNombre", proveedorNombre);
        m.put("activo", activo);
        m.put("fechaRegistro", fechaRegistro);
        return m;
    }
}
