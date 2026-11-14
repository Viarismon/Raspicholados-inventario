package co.sena.raspicholados.api.modelo;

import co.sena.raspicholados.api.json.ConvertibleJson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Categoría de productos (Raspados, Helados, Toppings, Insumos, Empaques...).
 * El nombre es único: la tabla producto lo referencia con una llave foránea
 * ON UPDATE CASCADE, de modo que renombrar una categoría actualiza sus productos.
 */
public class Categoria implements ConvertibleJson {

    private long id;
    private String nombre;
    private String descripcion;
    private int cantidadProductos;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(int cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public Categoria copia() {
        Categoria c = new Categoria();
        c.id = id;
        c.nombre = nombre;
        c.descripcion = descripcion;
        c.cantidadProductos = cantidadProductos;
        return c;
    }

    @Override
    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("nombre", nombre);
        m.put("descripcion", descripcion);
        m.put("cantidadProductos", cantidadProductos);
        return m;
    }
}
