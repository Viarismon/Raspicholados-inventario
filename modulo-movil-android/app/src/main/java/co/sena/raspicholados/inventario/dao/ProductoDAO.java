package co.sena.raspicholados.inventario.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import co.sena.raspicholados.inventario.db.ProductoDBHelper;
import co.sena.raspicholados.inventario.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Módulo: Inventario (versión móvil)
 * Propósito: Capa de acceso a datos del módulo de inventario para
 *            Android. Aplica el mismo patrón DAO y los mismos nombres
 *            de método definidos en la evidencia EV01 (JDBC/MySQL),
 *            pero implementados sobre SQLite, la base de datos
 *            embebida propia de Android. Evidencia GA7-220501096-AA3-EV01.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-30
 */
public class ProductoDAO {

    private final ProductoDBHelper dbHelper;

    public ProductoDAO(Context context) {
        this.dbHelper = new ProductoDBHelper(context);
    }

    /**
     * Inserta un nuevo producto en el inventario (RF-01).
     */
    public boolean insertarProducto(Producto producto) {
        SQLiteDatabase baseDatos = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(ProductoDBHelper.COLUMNA_NOMBRE, producto.getNombreProducto());
        valores.put(ProductoDBHelper.COLUMNA_CATEGORIA, producto.getCategoria());
        valores.put(ProductoDBHelper.COLUMNA_UNIDAD_MEDIDA, producto.getUnidadMedida());
        valores.put(ProductoDBHelper.COLUMNA_STOCK_ACTUAL, producto.getStockActual());
        valores.put(ProductoDBHelper.COLUMNA_STOCK_MINIMO, producto.getStockMinimo());

        long idInsertado = baseDatos.insert(ProductoDBHelper.TABLA_PRODUCTO, null, valores);
        baseDatos.close();
        return idInsertado != -1;
    }

    /**
     * Consulta un producto específico por su identificador (RF-05).
     */
    public Producto consultarProductoPorId(int idProducto) {
        SQLiteDatabase baseDatos = dbHelper.getReadableDatabase();
        Producto producto = null;

        Cursor cursor = baseDatos.query(
                ProductoDBHelper.TABLA_PRODUCTO,
                null,
                ProductoDBHelper.COLUMNA_ID + " = ?",
                new String[]{String.valueOf(idProducto)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            producto = mapearProducto(cursor);
        }
        cursor.close();
        baseDatos.close();
        return producto;
    }

    /**
     * Lista todos los productos registrados en el inventario (RF-05).
     */
    public List<Producto> listarProductos() {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase baseDatos = dbHelper.getReadableDatabase();

        Cursor cursor = baseDatos.query(
                ProductoDBHelper.TABLA_PRODUCTO,
                null, null, null, null, null,
                ProductoDBHelper.COLUMNA_NOMBRE + " ASC"
        );

        while (cursor.moveToNext()) {
            listaProductos.add(mapearProducto(cursor));
        }
        cursor.close();
        baseDatos.close();
        return listaProductos;
    }

    /**
     * Actualiza los datos de un producto existente (equivalente a un
     * ajuste de entradas o salidas de inventario, RF-02 y RF-03).
     */
    public boolean actualizarProducto(Producto producto) {
        SQLiteDatabase baseDatos = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(ProductoDBHelper.COLUMNA_NOMBRE, producto.getNombreProducto());
        valores.put(ProductoDBHelper.COLUMNA_CATEGORIA, producto.getCategoria());
        valores.put(ProductoDBHelper.COLUMNA_UNIDAD_MEDIDA, producto.getUnidadMedida());
        valores.put(ProductoDBHelper.COLUMNA_STOCK_ACTUAL, producto.getStockActual());
        valores.put(ProductoDBHelper.COLUMNA_STOCK_MINIMO, producto.getStockMinimo());

        int filasAfectadas = baseDatos.update(
                ProductoDBHelper.TABLA_PRODUCTO,
                valores,
                ProductoDBHelper.COLUMNA_ID + " = ?",
                new String[]{String.valueOf(producto.getIdProducto())}
        );
        baseDatos.close();
        return filasAfectadas > 0;
    }

    /**
     * Elimina un producto del inventario según su identificador.
     */
    public boolean eliminarProducto(int idProducto) {
        SQLiteDatabase baseDatos = dbHelper.getWritableDatabase();

        int filasAfectadas = baseDatos.delete(
                ProductoDBHelper.TABLA_PRODUCTO,
                ProductoDBHelper.COLUMNA_ID + " = ?",
                new String[]{String.valueOf(idProducto)}
        );
        baseDatos.close();
        return filasAfectadas > 0;
    }

    /**
     * Convierte la fila actual del cursor en un objeto Producto.
     */
    private Producto mapearProducto(Cursor cursor) {
        return new Producto(
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_CATEGORIA)),
                cursor.getString(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_UNIDAD_MEDIDA)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_STOCK_ACTUAL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductoDBHelper.COLUMNA_STOCK_MINIMO))
        );
    }
}
