package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroMovimientos;
import co.sena.raspicholados.api.repositorio.MovimientoRepositorio;
import co.sena.raspicholados.api.repositorio.OperacionStock;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;
import co.sena.raspicholados.api.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del repositorio de movimientos. */
public class MovimientoRepositorioMemoria implements MovimientoRepositorio {

    private final BaseDatosMemoria bd;

    public MovimientoRepositorioMemoria(BaseDatosMemoria bd) {
        this.bd = bd;
    }

    @Override
    public Movimiento registrar(long productoId, String tipo, int cantidad, String motivo,
                                UsuarioAutenticado usuario, OperacionStock operacion) {
        // El bloque sincronizado cumple el papel de la transacción con
        // SELECT ... FOR UPDATE de la versión MySQL.
        synchronized (bd.CANDADO) {
            Producto producto = bd.productos.get(productoId);
            int anterior = producto.getStockActual();
            int nuevo = operacion.calcularNuevoStock(anterior); // puede lanzar ApiException
            producto.setStockActual(nuevo);

            Movimiento m = new Movimiento();
            m.setId(++bd.secuenciaMovimiento);
            m.setProductoId(productoId);
            m.setProductoNombre(producto.getNombre());
            m.setTipo(tipo);
            m.setCantidad(cantidad);
            m.setStockAnterior(anterior);
            m.setStockResultante(nuevo);
            m.setMotivo(motivo);
            m.setUsuarioId(usuario.getId());
            m.setUsuarioNombre(usuario.getNombreUsuario());
            m.setFecha(Fechas.ahora());
            bd.movimientos.put(m.getId(), m);
            return m;
        }
    }

    @Override
    public List<Movimiento> listar(FiltroMovimientos filtro) {
        synchronized (bd.CANDADO) {
            List<Movimiento> lista = new ArrayList<Movimiento>();
            // Recorrido descendente: los más recientes primero.
            for (Movimiento m : bd.movimientos.descendingMap().values()) {
                if (filtro.productoId != null && m.getProductoId() != filtro.productoId) {
                    continue;
                }
                if (filtro.tipo != null && !m.getTipo().equals(filtro.tipo)) {
                    continue;
                }
                String dia = m.getFecha().substring(0, 10);
                if (filtro.desde != null && dia.compareTo(filtro.desde) < 0) {
                    continue;
                }
                if (filtro.hasta != null && dia.compareTo(filtro.hasta) > 0) {
                    continue;
                }
                lista.add(m);
                if (lista.size() >= filtro.limite) {
                    break;
                }
            }
            return lista;
        }
    }

    @Override
    public Movimiento buscarPorId(long id) {
        synchronized (bd.CANDADO) {
            return bd.movimientos.get(id);
        }
    }
}
