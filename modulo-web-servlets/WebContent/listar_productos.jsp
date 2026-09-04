<%--
  Módulo: Inventario (capa web)
  Propósito: Vista JSP que lista los productos del inventario y ofrece
             enlaces para crear, editar y eliminar (evidencia
             GA7-220501096-AA2-EV02).
  Autora: Viviana Arias Montilla
  Fecha de creación: 2026-08-28
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="co.sena.raspicholados.inventario.modelo.Producto" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>RASPICHOLADOS - Inventario</title>
    <link rel="stylesheet" href="estilos.css">
</head>
<body>

<header>
    <h1>RASPICHOLADOS</h1>
    <p>Módulo de inventario</p>
</header>

<main>
    <div class="barra-acciones">
        <a class="boton" href="productos?accion=nuevo">+ Nuevo producto</a>
    </div>

    <%--
      Elemento JSP: scriptlet para recuperar el atributo enviado por el
      servlet y controlar la lógica de la tabla.
    --%>
    <%
        List<Producto> listaProductos = (List<Producto>) request.getAttribute("listaProductos");
    %>

    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Unidad</th>
            <th>Stock actual</th>
            <th>Stock mínimo</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            if (listaProductos != null && !listaProductos.isEmpty()) {
                for (Producto productoActual : listaProductos) {
        %>
        <tr class="<%= productoActual.tieneStockBajo() ? "fila-alerta" : "" %>">
            <td><%= productoActual.getIdProducto() %></td>
            <td><%= productoActual.getNombreProducto() %></td>
            <td><%= productoActual.getCategoria() %></td>
            <td><%= productoActual.getUnidadMedida() %></td>
            <td><%= productoActual.getStockActual() %></td>
            <td><%= productoActual.getStockMinimo() %></td>
            <td><%= productoActual.tieneStockBajo() ? "Stock bajo" : "Normal" %></td>
            <td>
                <a href="productos?accion=editar&id=<%= productoActual.getIdProducto() %>">Editar</a>
                &nbsp;|&nbsp;
                <a href="productos?accion=eliminar&id=<%= productoActual.getIdProducto() %>"
                   onclick="return confirm('¿Eliminar este producto del inventario?');">Eliminar</a>
            </td>
        </tr>
        <%
                }
            } else {
        %>
        <tr>
            <td colspan="8">No hay productos registrados todavía.</td>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>

    <p class="pie-tabla">
        Total de productos listados:
        <%-- Elemento JSP: Expression Language (EL) --%>
        ${listaProductos != null ? listaProductos.size() : 0}
    </p>
</main>

</body>
</html>
