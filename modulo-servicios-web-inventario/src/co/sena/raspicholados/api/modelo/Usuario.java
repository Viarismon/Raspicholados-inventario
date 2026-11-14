package co.sena.raspicholados.api.modelo;

import co.sena.raspicholados.api.json.ConvertibleJson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Usuario del sistema (administrador o empleado de la heladería).
 * El hash de la contraseña NUNCA se incluye en la representación JSON.
 */
public class Usuario implements ConvertibleJson {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_EMPLEADO = "EMPLEADO";

    private long id;
    private String nombreUsuario;
    private String nombreCompleto;
    private String rol;
    private String hashContrasena;
    private boolean activo = true;
    private String fechaRegistro;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getHashContrasena() { return hashContrasena; }
    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("nombreUsuario", nombreUsuario);
        m.put("nombreCompleto", nombreCompleto);
        m.put("rol", rol);
        m.put("activo", activo);
        m.put("fechaRegistro", fechaRegistro);
        return m;
    }
}
