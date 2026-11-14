package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroMovimientos;
import co.sena.raspicholados.api.repositorio.MovimientoRepositorio;
import co.sena.raspicholados.api.repositorio.OperacionStock;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;
import co.sena.raspicholados.api.util.Fechas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio de los movimientos de inventario (corazón del sistema).
 *
 *   ENTRADA: cantidad >= 1, stock = stock + cantidad
 *   SALIDA : cantidad >= 1, stock = stock - cantidad; si no alcanza -> 409 STOCK_INSUFICIENTE
 *   AJUSTE : cantidad >= 0 es el conteo físico; stock = cantidad; motivo obligatorio
 */
public class MovimientoServicio {

    public static final int MAXIMO_STOCK = 1000000;

    private final MovimientoRepositorio movimientos;
    private final ProductoRepositorio productos;

    public MovimientoServicio(MovimientoRepositorio movimientos, ProductoRepositorio productos) {
        this.movimientos = movimientos;
        this.productos = productos;
    }

    /** Registra un movimiento enviado por el cliente y devuelve movimiento + producto actualizado. */
    public Map<String, Object> registrar(Map<String, Object> datos, UsuarioAutenticado usuario) {
        Validador v = new Validador(datos);
        Long productoId = v.id("productoId", true);
        String tipo = v.opcion("tipo", true, Movimiento.ENTRADA, Movimiento.SALIDA, Movimiento.AJUSTE);
        int minimo = Movimiento.AJUSTE.equals(tipo) ? 0 : 1;
        Integer cantidad = v.entero("cantidad", true, minimo, MAXIMO_STOCK);
        String motivo = v.texto("motivo", Movimiento.AJUSTE.equals(tipo), 3, 200);
        v.validar();

        Movimiento m = registrarInterno(productoId, tipo, cantidad, motivo, usuario);
        Producto producto = productos.buscarPorId(productoId);

        Map<String, Object> resultado = new LinkedHashMap<String, Object>();
        resultado.put("movimiento", m);
        resultado.put("producto", producto);
        if (!Producto.ESTADO_NORMAL.equals(producto.getEstadoStock())) {
            resultado.put("alerta", "El producto '" + producto.getNombre() + "' quedó en estado "
                    + producto.getEstadoStock() + " (stock " + producto.getStockActual()
                    + ", mínimo " + producto.getStockMinimo() + ")");
        }
        return resultado;
    }

    /** Registro usado también por ProductoServicio para el inventario inicial. */
    Movimiento registrarInterno(final long productoId, final String tipo, final int cantidad,
                                String motivo, UsuarioAutenticado usuario) {
        final Producto producto = productos.buscarPorId(productoId);
        if (producto == null) {
            throw ApiException.noEncontrado("un producto", productoId);
        }
        if (!producto.isActivo()) {
            throw ApiException.conflicto("PRODUCTO_INACTIVO",
                    "El producto " + productoId + " fue dado de baja y no admite movimientos");
        }
        OperacionStock operacion = new OperacionStock() {
            @Override
            public int calcularNuevoStock(int actual) {
                if (Movimiento.ENTRADA.equals(tipo)) {
                    if ((long) actual + cantidad > MAXIMO_STOCK) {
                        throw ApiException.conflicto("STOCK_EXCEDE_MAXIMO",
                                "La entrada dejaría el stock por encima del máximo permitido (" + MAXIMO_STOCK + ")");
                    }
                    return actual + cantidad;
                }
                if (Movimiento.SALIDA.equals(tipo)) {
                    if (cantidad > actual) {
                        List<Map<String, Object>> detalles = new ArrayList<Map<String, Object>>();
                        Map<String, Object> d = new LinkedHashMap<String, Object>();
                        d.put("campo", "cantidad");
                        d.put("mensaje", "Solicitado: " + cantidad + ", disponible: " + actual);
                        detalles.add(d);
                        throw new ApiException(409, "STOCK_INSUFICIENTE",
                                "No hay stock suficiente de '" + producto.getNombre() + "'", detalles);
                    }
                    return actual - cantidad;
                }
                return cantidad; // AJUSTE: el conteo físico pasa a ser el stock
            }
        };
        return movimientos.registrar(productoId, tipo, cantidad, motivo, usuario, operacion);
    }

    public List<Movimiento> listar(Long productoId, String tipo, String desde, String hasta, Long limite) {
        FiltroMovimientos filtro = new FiltroMovimientos();
        filtro.productoId = productoId;
        if (tipo != null) {
            filtro.tipo = tipo.toUpperCase();
            if (!filtro.tipo.equals(Movimiento.ENTRADA) && !filtro.tipo.equals(Movimiento.SALIDA)
                    && !filtro.tipo.equals(Movimiento.AJUSTE)) {
                throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO",
                        "El parámetro 'tipo' debe ser ENTRADA, SALIDA o AJUSTE");
            }
        }
        if (desde != null && !Fechas.esFechaValida(desde)) {
            throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO", "El parámetro 'desde' debe tener formato AAAA-MM-DD");
        }
        if (hasta != null && !Fechas.esFechaValida(hasta)) {
            throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO", "El parámetro 'hasta' debe tener formato AAAA-MM-DD");
        }
        if (desde != null && hasta != null && desde.compareTo(hasta) > 0) {
            throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO", "'desde' no puede ser posterior a 'hasta'");
        }
        filtro.desde = desde;
        filtro.hasta = hasta;
        if (limite != null) {
            if (limite < 1 || limite > 500) {
                throw ApiException.solicitudInvalida("PARAMETRO_INVALIDO", "'limite' debe estar entre 1 y 500");
            }
            filtro.limite = limite.intValue();
        }
        return movimientos.listar(filtro);
    }

    public Movimiento obtener(long id) {
        Movimiento m = movimientos.buscarPorId(id);
        if (m == null) {
            throw ApiException.noEncontrado("un movimiento", id);
        }
        return m;
    }
}
