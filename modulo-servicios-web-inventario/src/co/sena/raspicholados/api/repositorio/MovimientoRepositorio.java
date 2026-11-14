package co.sena.raspicholados.api.repositorio;

import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;

import java.util.List;

/** Contrato de persistencia de movimientos de inventario. */
public interface MovimientoRepositorio {

    /**
     * Registra un movimiento de forma ATÓMICA: bloquea el producto, calcula el
     * nuevo stock con la operación recibida, actualiza el producto e inserta
     * el movimiento. Si la operación lanza una excepción no se modifica nada.
     * Así dos ventas simultáneas no pueden dejar el stock en negativo.
     */
    Movimiento registrar(long productoId, String tipo, int cantidad, String motivo,
                         UsuarioAutenticado usuario, OperacionStock operacion);

    List<Movimiento> listar(FiltroMovimientos filtro);

    Movimiento buscarPorId(long id);
}
