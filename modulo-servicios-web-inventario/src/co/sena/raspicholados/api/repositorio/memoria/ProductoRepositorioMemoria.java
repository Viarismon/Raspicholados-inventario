package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del repositorio de productos. */
public class ProductoRepositorioMemoria implements ProductoRepositorio {

    private final BaseDatosMemoria bd;

    public ProductoRepositorioMemoria(BaseDatosMemoria bd) {
        this.bd = bd;
    }

    @Override
    public List<Producto> listar(FiltroProductos filtro) {
        synchronized (bd.CANDADO) {
            List<Producto> lista = new ArrayList<Producto>();
            for (Producto p : bd.productos.values()) {
                if (!p.isActivo()) {
                    continue;
                }
                if (filtro.categoria != null && !p.getCategoria().equalsIgnoreCase(filtro.categoria)) {
                    continue;
                }
                if (filtro.texto != null
                        && !p.getNombre().toLowerCase().contains(filtro.texto.toLowerCase())) {
                    continue;
                }
                if (filtro.proveedorId != null && !filtro.proveedorId.equals(p.getProveedorId())) {
                    continue;
                }
                if (filtro.soloStockBajo && p.getStockActual() > p.getStockMinimo()) {
                    continue;
                }
                lista.add(bd.conProveedor(p));
            }
            return lista;
        }
    }

    @Override
    public Producto buscarPorId(long id) {
        synchronized (bd.CANDADO) {
            Producto p = bd.productos.get(id);
            return p == null ? null : bd.conProveedor(p);
        }
    }

    @Override
    public Producto buscarActivoPorNombre(String nombre) {
        synchronized (bd.CANDADO) {
            for (Producto p : bd.productos.values()) {
                if (p.isActivo() && p.getNombre().equalsIgnoreCase(nombre)) {
                    return bd.conProveedor(p);
                }
            }
            return null;
        }
    }

    @Override
    public Producto guardar(Producto producto) {
        synchronized (bd.CANDADO) {
            Producto nuevo = producto.copia();
            nuevo.setId(++bd.secuenciaProducto);
            nuevo.setStockActual(0);
            nuevo.setActivo(true);
            nuevo.setFechaRegistro(Fechas.ahora());
            bd.productos.put(nuevo.getId(), nuevo);
            return bd.conProveedor(nuevo);
        }
    }

    @Override
    public Producto actualizar(Producto producto) {
        synchronized (bd.CANDADO) {
            Producto actual = bd.productos.get(producto.getId());
            actual.setNombre(producto.getNombre());
            actual.setCategoria(producto.getCategoria());
            actual.setUnidadMedida(producto.getUnidadMedida());
            actual.setStockMinimo(producto.getStockMinimo());
            actual.setPrecioUnitario(producto.getPrecioUnitario());
            actual.setProveedorId(producto.getProveedorId());
            return bd.conProveedor(actual);
        }
    }

    @Override
    public void desactivar(long id) {
        synchronized (bd.CANDADO) {
            Producto p = bd.productos.get(id);
            if (p != null) {
                p.setActivo(false);
            }
        }
    }
}
