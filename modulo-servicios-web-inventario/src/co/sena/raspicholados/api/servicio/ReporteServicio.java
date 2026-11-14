package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.FiltroMovimientos;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import co.sena.raspicholados.api.repositorio.MovimientoRepositorio;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.util.Fechas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas de apoyo a la toma de decisiones: alertas y resumen del inventario. */
public class ReporteServicio {

    private final ProductoRepositorio productos;
    private final MovimientoRepositorio movimientos;

    public ReporteServicio(ProductoRepositorio productos, MovimientoRepositorio movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }

    /**
     * Productos en estado AGOTADO o BAJO, con la cantidad sugerida a pedir
     * para volver al doble del mínimo. Primero los agotados y luego
     * los de menor proporción entre stock actual y mínimo.
     */
    public List<Map<String, Object>> alertasStockBajo() {
        FiltroProductos filtro = new FiltroProductos();
        filtro.soloStockBajo = true;
        List<Producto> bajos = new ArrayList<Producto>(productos.listar(filtro));
        Collections.sort(bajos, new Comparator<Producto>() {
            @Override
            public int compare(Producto a, Producto b) {
                // Primero los agotados; luego, el que tenga menor proporción stock/mínimo.
                boolean agotadoA = a.getStockActual() <= 0;
                boolean agotadoB = b.getStockActual() <= 0;
                if (agotadoA != agotadoB) {
                    return agotadoA ? -1 : 1;
                }
                double proporcionA = a.getStockActual() / (double) Math.max(a.getStockMinimo(), 1);
                double proporcionB = b.getStockActual() / (double) Math.max(b.getStockMinimo(), 1);
                return Double.compare(proporcionA, proporcionB);
            }
        });
        List<Map<String, Object>> alertas = new ArrayList<Map<String, Object>>();
        for (Producto p : bajos) {
            Map<String, Object> a = new LinkedHashMap<String, Object>();
            a.put("productoId", p.getId());
            a.put("nombre", p.getNombre());
            a.put("categoria", p.getCategoria());
            a.put("estadoStock", p.getEstadoStock());
            a.put("stockActual", p.getStockActual());
            a.put("stockMinimo", p.getStockMinimo());
            a.put("cantidadSugeridaPedido", Math.max(p.getStockMinimo() * 2 - p.getStockActual(), 1));
            a.put("unidadMedida", p.getUnidadMedida());
            a.put("proveedorId", p.getProveedorId());
            a.put("proveedorNombre", p.getProveedorNombre());
            alertas.add(a);
        }
        return alertas;
    }

    public Map<String, Object> resumen() {
        List<Producto> activos = productos.listar(new FiltroProductos());
        long unidades = 0;
        int agotados = 0;
        int bajos = 0;
        BigDecimal valor = BigDecimal.ZERO;
        Map<String, Integer> porCategoria = new LinkedHashMap<String, Integer>();
        for (Producto p : activos) {
            unidades += p.getStockActual();
            valor = valor.add(p.getValorInventario());
            if (Producto.ESTADO_AGOTADO.equals(p.getEstadoStock())) {
                agotados++;
            } else if (Producto.ESTADO_BAJO.equals(p.getEstadoStock())) {
                bajos++;
            }
            Integer actual = porCategoria.get(p.getCategoria());
            porCategoria.put(p.getCategoria(), actual == null ? 1 : actual + 1);
        }

        FiltroMovimientos hoy = new FiltroMovimientos();
        hoy.desde = Fechas.hoy();
        hoy.hasta = Fechas.hoy();
        hoy.limite = 500;
        List<Movimiento> movimientosHoy = movimientos.listar(hoy);
        int entradas = 0;
        int salidas = 0;
        for (Movimiento m : movimientosHoy) {
            if (Movimiento.ENTRADA.equals(m.getTipo())) {
                entradas += m.getCantidad();
            } else if (Movimiento.SALIDA.equals(m.getTipo())) {
                salidas += m.getCantidad();
            }
        }

        FiltroMovimientos ultimos = new FiltroMovimientos();
        ultimos.limite = 5;

        Map<String, Object> r = new LinkedHashMap<String, Object>();
        r.put("fechaCorte", Fechas.ahora());
        r.put("totalProductosActivos", activos.size());
        r.put("totalUnidadesEnStock", unidades);
        r.put("valorTotalInventario", valor.setScale(2, java.math.RoundingMode.HALF_UP));
        r.put("productosAgotados", agotados);
        r.put("productosStockBajo", bajos);
        r.put("productosPorCategoria", porCategoria);
        r.put("movimientosHoy", movimientosHoy.size());
        r.put("unidadesEntradasHoy", entradas);
        r.put("unidadesSalidasHoy", salidas);
        r.put("ultimosMovimientos", movimientos.listar(ultimos));
        return r;
    }
}
