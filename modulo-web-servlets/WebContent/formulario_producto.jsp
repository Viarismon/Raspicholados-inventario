<%--
  Módulo: Inventario (capa web)
  Propósito: Formulario HTML/JSP para insertar o editar un producto
             del inventario. El mismo formulario sirve para ambas
             operaciones, enviando los datos por POST al ProductoServlet
             (evidencia GA7-220501096-AA2-EV02).
  Autora: Viviana Arias Montilla
  Fecha de creación: 2026-08-28
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Elemento JSP: acción jsp:useBean. Si el servlet ya dejó un
     "producto" en el request (modo edición), lo reutiliza; si no
     existe, crea uno vacío (modo creación). --%>
<jsp:useBean id="producto" scope="request" class="co.sena.raspicholados.inventario.modelo.Producto" />

<%
    // Elemento JSP: scriptlet para decidir si el formulario está en
    // modo edición (idProducto > 0) o en modo creación.
    boolean esEdicion = producto.getIdProducto() > 0;
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>RASPICHOLADOS - <%= esEdicion ? "Editar producto" : "Nuevo producto" %></title>
    <link rel="stylesheet" href="estilos.css">
</head>
<body>

<header>
    <h1>RASPICHOLADOS</h1>
    <p>Módulo de inventario</p>
</header>

<main>
    <h2><%= esEdicion ? "Editar producto" : "Registrar nuevo producto" %></h2>

    <%-- El formulario envía los datos por POST al mismo servlet. --%>
    <form action="productos" method="post">

        <% if (esEdicion) { %>
        <input type="hidden" name="idProducto" value='<jsp:getProperty name="producto" property="idProducto" />'>
        <% } %>

        <label for="nombreProducto">Nombre del producto</label>
        <input type="text" id="nombreProducto" name="nombreProducto"
               value='<jsp:getProperty name="producto" property="nombreProducto" />' required>

        <label for="categoria">Categoría</label>
        <input type="text" id="categoria" name="categoria"
               value='<jsp:getProperty name="producto" property="categoria" />' required>

        <label for="unidadMedida">Unidad de medida</label>
        <input type="text" id="unidadMedida" name="unidadMedida"
               value='<jsp:getProperty name="producto" property="unidadMedida" />' required>

        <label for="stockActual">Stock actual</label>
        <input type="number" id="stockActual" name="stockActual" min="0"
               value="<%= esEdicion ? producto.getStockActual() : 0 %>" required>

        <label for="stockMinimo">Stock mínimo</label>
        <input type="number" id="stockMinimo" name="stockMinimo" min="0"
               value="<%= esEdicion ? producto.getStockMinimo() : 0 %>" required>

        <div class="acciones-formulario">
            <button type="submit" class="boton">
                <%= esEdicion ? "Guardar cambios" : "Registrar producto" %>
            </button>
            <a class="boton boton-secundario" href="productos">Cancelar</a>
        </div>
    </form>
</main>

</body>
</html>
