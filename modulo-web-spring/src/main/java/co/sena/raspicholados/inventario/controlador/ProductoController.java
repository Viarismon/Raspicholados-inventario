package co.sena.raspicholados.inventario.controlador;

import co.sena.raspicholados.inventario.modelo.Producto;
import co.sena.raspicholados.inventario.servicio.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Módulo: Inventario (versión web con framework)
 * Propósito: Controlador Spring MVC del módulo de inventario. Cumple
 *            la misma función que ProductoServlet en la evidencia
 *            EV02, pero usando las anotaciones del framework
 *            (@GetMapping, @PostMapping) en vez de sobrescribir
 *            manualmente doGet/doPost, y delegando las vistas a
 *            plantillas Thymeleaf. Evidencia GA7-220501096-AA3-EV01
 *            (enfoque web con framework).
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-09-02
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Lista todos los productos del inventario (equivalente al
     * doGet sin parámetros de la evidencia EV02).
     */
    @GetMapping
    public String listarProductos(Model modelo) {
        modelo.addAttribute("listaProductos", productoService.listarProductos());
        return "listar_productos";
    }

    /**
     * Muestra el formulario vacío para registrar un producto nuevo.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model modelo) {
        modelo.addAttribute("producto", new Producto());
        modelo.addAttribute("esEdicion", false);
        return "formulario_producto";
    }

    /**
     * Carga un producto existente y muestra el formulario en modo
     * edición.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Integer idProducto, Model modelo) {
        Producto producto = productoService.consultarProductoPorId(idProducto);
        modelo.addAttribute("producto", producto);
        modelo.addAttribute("esEdicion", true);
        return "formulario_producto";
    }

    /**
     * Recibe el formulario (inserción o actualización) y redirige al
     * listado. Spring enlaza automáticamente los campos del formulario
     * HTML al objeto Producto mediante @ModelAttribute.
     */
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        productoService.guardarProducto(producto);
        return "redirect:/productos";
    }

    /**
     * Elimina un producto y redirige al listado (patrón
     * Post/Redirect/Get, igual que en la evidencia EV02).
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer idProducto) {
        productoService.eliminarProducto(idProducto);
        return "redirect:/productos";
    }
}
