package co.sena.raspicholados.api.modelo;

import co.sena.raspicholados.api.json.ConvertibleJson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Movimiento de inventario: toda variación del stock queda registrada aquí
 * (trazabilidad). Tipos:
 *   ENTRADA -> compra o recepción de mercancía (suma al stock)
 *   SALIDA  -> venta, consumo o merma (resta del stock)
 *   AJUSTE  -> conteo físico: el stock pasa a ser exactamente la cantidad
 */
public class Movimiento implements ConvertibleJson {

    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";
    public static final String AJUSTE = "AJUSTE";

    private long id;
    private long productoId;
    private String productoNombre;
    private String tipo;
    private int cantidad;
    private int stockAnterior;
    private int stockResultante;
    private String motivo;
    private long usuarioId;
    private String usuarioNombre;
    private String fecha;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getProductoId() { return productoId; }
    public void setProductoId(long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(int stockAnterior) { this.stockAnterior = stockAnterior; }
    public int getStockResultante() { return stockResultante; }
    public void setStockResultante(int stockResultante) { this.stockResultante = stockResultante; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    @Override
    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("productoId", productoId);
        m.put("productoNombre", productoNombre);
        m.put("tipo", tipo);
        m.put("cantidad", cantidad);
        m.put("stockAnterior", stockAnterior);
        m.put("stockResultante", stockResultante);
        m.put("diferencia", stockResultante - stockAnterior);
        m.put("motivo", motivo);
        m.put("usuarioId", usuarioId);
        m.put("usuarioNombre", usuarioNombre);
        m.put("fecha", fecha);
        return m;
    }
}
