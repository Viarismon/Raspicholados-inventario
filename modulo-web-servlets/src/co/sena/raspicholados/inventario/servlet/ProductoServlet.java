package co.sena.raspicholados.inventario.servlet;

import co.sena.raspicholados.inventario.dao.ProductoDAO;
import co.sena.raspicholados.inventario.dao.ProductoDAOImpl;
import co.sena.raspicholados.inventario.modelo.Producto;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Módulo: Inventario (capa web)
 * Propósito: Servlet controlador del módulo de inventario. Atiende
 *            peticiones GET (listar, cargar formulario, eliminar) y
 *            POST (guardar inserciones y actualizaciones), evidencia
 *            GA7-220501096-AA2-EV02.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
@WebServlet("/productos")
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ProductoDAO productoDAO = new ProductoDAOImpl();

    /**
     * Atiende las peticiones GET del módulo:
     * - sin parámetros: lista todos los productos.
     * - accion=nuevo: muestra el formulario vacío para insertar.
     * - accion=editar&id=X: carga el producto y muestra el formulario.
     * - accion=eliminar&id=X: elimina el producto y redirige al listado.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if (accion == null) {
            mostrarListadoProductos(request, response);

        } else if ("nuevo".equals(accion)) {
            RequestDispatcher despachador = request.getRequestDispatcher("formulario_producto.jsp");
            despachador.forward(request, response);

        } else if ("editar".equals(accion)) {
            int idProducto = Integer.parseInt(request.getParameter("id"));
            Producto producto = productoDAO.consultarProductoPorId(idProducto);
            request.setAttribute("producto", producto);
            RequestDispatcher despachador = request.getRequestDispatcher("formulario_producto.jsp");
            despachador.forward(request, response);

        } else if ("eliminar".equals(accion)) {
            int idProducto = Integer.parseInt(request.getParameter("id"));
            productoDAO.eliminarProducto(idProducto);
            // Patrón Post/Redirect/Get: tras la acción se redirige con GET
            // para evitar que recargar la página repita la operación.
            response.sendRedirect("productos");

        } else {
            mostrarListadoProductos(request, response);
        }
    }

    /**
     * Atiende las peticiones POST del módulo: recibe los datos del
     * formulario HTML y decide si debe insertar un producto nuevo o
     * actualizar uno existente, según venga o no el idProducto.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idProductoTexto = request.getParameter("idProducto");
        String nombreProducto = request.getParameter("nombreProducto");
        String categoria = request.getParameter("categoria");
        String unidadMedida = request.getParameter("unidadMedida");
        int stockActual = Integer.parseInt(request.getParameter("stockActual"));
        int stockMinimo = Integer.parseInt(request.getParameter("stockMinimo"));

        Producto producto = new Producto(nombreProducto, categoria, unidadMedida, stockActual, stockMinimo);

        boolean esActualizacion = idProductoTexto != null && !idProductoTexto.trim().isEmpty();

        if (esActualizacion) {
            producto.setIdProducto(Integer.parseInt(idProductoTexto));
            productoDAO.actualizarProducto(producto);
        } else {
            productoDAO.insertarProducto(producto);
        }

        // Tras procesar el POST, se redirige con GET al listado
        // (patrón Post/Redirect/Get).
        response.sendRedirect("productos");
    }

    /**
     * Consulta todos los productos y los envía a la vista de listado.
     */
    private void mostrarListadoProductos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Producto> listaProductos = productoDAO.listarProductos();
        request.setAttribute("listaProductos", listaProductos);
        RequestDispatcher despachador = request.getRequestDispatcher("listar_productos.jsp");
        despachador.forward(request, response);
    }
}
