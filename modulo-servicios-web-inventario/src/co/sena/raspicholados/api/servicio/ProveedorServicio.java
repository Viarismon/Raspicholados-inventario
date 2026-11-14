package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Proveedor;
import co.sena.raspicholados.api.repositorio.ProveedorRepositorio;

import java.util.List;
import java.util.Map;

/** Reglas de negocio de los proveedores. */
public class ProveedorServicio {

    private static final String REGEX_NIT = "^[0-9]{6,12}(-[0-9])?$";
    private static final String REGEX_TELEFONO = "^[0-9+() -]{7,20}$";
    private static final String REGEX_CORREO = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final ProveedorRepositorio proveedores;

    public ProveedorServicio(ProveedorRepositorio proveedores) {
        this.proveedores = proveedores;
    }

    public List<Proveedor> listar() {
        return proveedores.listar();
    }

    public Proveedor obtener(long id) {
        Proveedor p = proveedores.buscarPorId(id);
        if (p == null) {
            throw ApiException.noEncontrado("un proveedor", id);
        }
        return p;
    }

    public Proveedor crear(Map<String, Object> datos) {
        Proveedor p = leer(datos);
        if (proveedores.buscarPorNit(p.getNit()) != null) {
            throw duplicado(p.getNit());
        }
        return proveedores.guardar(p);
    }

    public Proveedor actualizar(long id, Map<String, Object> datos) {
        obtener(id);
        Proveedor p = leer(datos);
        Proveedor mismoNit = proveedores.buscarPorNit(p.getNit());
        if (mismoNit != null && mismoNit.getId() != id) {
            throw duplicado(p.getNit());
        }
        p.setId(id);
        return proveedores.actualizar(p);
    }

    public void eliminar(long id) {
        Proveedor p = obtener(id);
        int activos = proveedores.contarProductosActivos(id);
        if (activos > 0) {
            throw ApiException.conflicto("PROVEEDOR_CON_PRODUCTOS",
                    "No se puede eliminar a '" + p.getNombre() + "' porque surte " + activos
                            + " producto(s) activo(s). Asigne otro proveedor a esos productos primero.");
        }
        proveedores.eliminar(id);
    }

    private static Proveedor leer(Map<String, Object> datos) {
        Validador v = new Validador(datos);
        Proveedor p = new Proveedor();
        p.setNit(v.patron("nit", true, 14, REGEX_NIT, "Formato de NIT inválido. Ejemplo: 900123456-1"));
        p.setNombre(v.texto("nombre", true, 3, 100));
        p.setTelefono(v.patron("telefono", false, 20, REGEX_TELEFONO, "Teléfono inválido (7 a 20 dígitos)"));
        p.setCorreo(v.patron("correo", false, 100, REGEX_CORREO, "Correo electrónico inválido"));
        p.setDireccion(v.texto("direccion", false, 0, 150));
        v.validar();
        return p;
    }

    private static ApiException duplicado(String nit) {
        return ApiException.conflicto("PROVEEDOR_DUPLICADO", "Ya existe un proveedor con el NIT " + nit);
    }
}
