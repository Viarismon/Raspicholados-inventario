package co.sena.raspicholados.inventario.principal;

import co.sena.raspicholados.inventario.dao.ProductoDAO;
import co.sena.raspicholados.inventario.dao.ProductoDAOImpl;
import co.sena.raspicholados.inventario.modelo.Producto;

/**
 * Módulo: Inventario
 * Propósito: Ejecutar y documentar las pruebas de validación de datos
 *            sobre el módulo de inventario, exigidas por la evidencia
 *            GA7-220501096-AA2-EV01.
 * Autora: Viviana Arias Montilla
 */
public class PruebaValidaciones {

    public static void main(String[] argumentos) {

        ProductoDAO productoDAO = new ProductoDAOImpl();

        System.out.println("========== PRUEBA 1: Nombre vacio ==========");
        try {
            Producto p1 = new Producto("", "Insumo", "Botella", 10, 5);
            boolean resultado = productoDAO.insertarProducto(p1);
            System.out.println(resultado
                ? "RESULTADO: el sistema SI permitio guardar con nombre vacio (no hay validacion de campo obligatorio)."
                : "RESULTADO: el sistema rechazo el registro con nombre vacio.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 2: Caracteres especiales en nombre ==========");
        try {
            Producto p2 = new Producto("Cholado #1 @@ %%%", "Insumo", "Botella", 10, 5);
            boolean resultado = productoDAO.insertarProducto(p2);
            System.out.println(resultado
                ? "RESULTADO: el sistema guardo el nombre con caracteres especiales sin error."
                : "RESULTADO: no se pudo insertar el producto.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 3: Longitud excedida en nombre (mas de 80 caracteres) ==========");
        try {
            String nombreLargo = "X".repeat(120);
            Producto p3 = new Producto(nombreLargo, "Insumo", "Botella", 10, 5);
            boolean resultado = productoDAO.insertarProducto(p3);
            System.out.println(resultado
                ? "RESULTADO: el sistema guardo el nombre largo (posible truncamiento silencioso)."
                : "RESULTADO: no se pudo insertar el producto.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 4: Texto en campo numerico ==========");
        System.out.println("RESULTADO: No aplica en tiempo de ejecucion. Java es un lenguaje fuertemente tipado:");
        System.out.println("el metodo insertarProducto exige un valor 'int' para stockActual y stockMinimo,");
        System.out.println("por lo que el compilador RECHAZA cualquier intento de pasar texto en ese campo.");
        System.out.println("Esta validacion ocurre en tiempo de COMPILACION, antes de ejecutar el programa.");

        System.out.println("\n========== PRUEBA 5: Numero negativo en stock ==========");
        try {
            Producto p5 = new Producto("Producto stock negativo", "Insumo", "Botella", -10, 5);
            boolean resultado = productoDAO.insertarProducto(p5);
            System.out.println(resultado
                ? "RESULTADO: el sistema SI permitio guardar un stock negativo (-10). No hay validacion de rango."
                : "RESULTADO: el sistema rechazo el stock negativo.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 6: Valor decimal en campo entero ==========");
        double valorDecimal = 12.5;
        int valorTruncado = (int) valorDecimal;
        System.out.println("Valor ingresado: " + valorDecimal + " -> convertido a entero: " + valorTruncado);
        try {
            Producto p6 = new Producto("Producto valor decimal", "Insumo", "Botella", valorTruncado, 5);
            boolean resultado = productoDAO.insertarProducto(p6);
            System.out.println(resultado
                ? "RESULTADO: el sistema trunco 12.5 a 12 automaticamente (conversion de double a int)."
                : "RESULTADO: no se pudo insertar el producto.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 7: Categoria vacia ==========");
        try {
            Producto p7 = new Producto("Producto sin categoria", "", "Botella", 10, 5);
            boolean resultado = productoDAO.insertarProducto(p7);
            System.out.println(resultado
                ? "RESULTADO: el sistema SI permitio guardar con categoria vacia (no hay validacion de campo obligatorio)."
                : "RESULTADO: el sistema rechazo el registro con categoria vacia.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion capturada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBA 8: Consultar un ID inexistente (9999) ==========");
        try {
            Producto p8 = productoDAO.consultarProductoPorId(9999);
            System.out.println(p8 != null
                ? "RESULTADO: se encontro un producto (inesperado)."
                : "RESULTADO: el sistema informo correctamente que no existe el producto (retorno null), sin lanzar una excepcion no controlada.");
        } catch (Exception e) {
            System.out.println("RESULTADO: excepcion NO controlada -> " + e.getMessage());
        }

        System.out.println("\n========== PRUEBAS 9 y 10 ==========");
        System.out.println("Las pruebas 9 (actualizar stock) y 10 (eliminar producto) ya se documentaron");
        System.out.println("con datos validos en la ejecucion de PruebaInventarioCRUD.java (pasos 4 y 5).");
    }
}