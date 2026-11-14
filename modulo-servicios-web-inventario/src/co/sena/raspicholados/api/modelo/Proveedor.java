package co.sena.raspicholados.api.modelo;

import co.sena.raspicholados.api.json.ConvertibleJson;

import java.util.LinkedHashMap;
import java.util.Map;

/** Proveedor de insumos de la heladería (frutas, lácteos, empaques...). */
public class Proveedor implements ConvertibleJson {

    private long id;
    private String nit;
    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Proveedor copia() {
        Proveedor p = new Proveedor();
        p.id = id;
        p.nit = nit;
        p.nombre = nombre;
        p.telefono = telefono;
        p.correo = correo;
        p.direccion = direccion;
        return p;
    }

    @Override
    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("nit", nit);
        m.put("nombre", nombre);
        m.put("telefono", telefono);
        m.put("correo", correo);
        m.put("direccion", direccion);
        return m;
    }
}
