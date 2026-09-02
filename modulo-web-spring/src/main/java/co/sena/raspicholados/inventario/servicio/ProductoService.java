package co.sena.raspicholados.inventario.servicio;

import co.sena.raspicholados.inventario.modelo.Producto;
import co.sena.raspicholados.inventario.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Módulo: Inventario (versión web con framework)
 * Propósito: Capa de lógica de negocio del módulo de inventario,
 *            equivalente a la capa "Lógica de negocio" definida en el
 *            informe de diseño de la solución de software. Usa
 *            inyección de dependencias de Spring para obtener el
 *            repositorio, en vez de instanciarlo manualmente.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-09-02
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto consultarProductoPorId(Integer idProducto) {
        Optional<Producto> productoEncontrado = productoRepository.findById(idProducto);
        return productoEncontrado.orElse(null);
    }

    public void guardarProducto(Producto producto) {
        productoRepository.save(producto);
    }

    public void eliminarProducto(Integer idProducto) {
        productoRepository.deleteById(idProducto);
    }
}
