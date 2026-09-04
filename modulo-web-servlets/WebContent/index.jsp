<%--
  Módulo: Inventario (capa web)
  Propósito: Página de bienvenida del módulo web. Redirige al listado
             de productos gestionado por el ProductoServlet.
  Autora: Viviana Arias Montilla
  Fecha de creación: 2026-08-28
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Redirección automática (petición GET) hacia el servlet principal
    // del módulo de inventario.
    response.sendRedirect("productos");
%>
