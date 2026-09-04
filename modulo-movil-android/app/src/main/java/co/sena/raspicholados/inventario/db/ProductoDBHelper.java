package co.sena.raspicholados.inventario.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Módulo: Inventario (versión móvil)
 * Propósito: Administra la creación y actualización de la base de
 *            datos SQLite embebida en el dispositivo, equivalente
 *            local al esquema raspicholados_db usado en las
 *            evidencias de escritorio y web (EV01 y EV02).
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-30
 */
public class ProductoDBHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BASE_DATOS = "raspicholados.db";
    private static final int VERSION_BASE_DATOS = 1;

    public static final String TABLA_PRODUCTO = "producto";
    public static final String COLUMNA_ID = "id_producto";
    public static final String COLUMNA_NOMBRE = "nombre_producto";
    public static final String COLUMNA_CATEGORIA = "categoria";
    public static final String COLUMNA_UNIDAD_MEDIDA = "unidad_medida";
    public static final String COLUMNA_STOCK_ACTUAL = "stock_actual";
    public static final String COLUMNA_STOCK_MINIMO = "stock_minimo";

    private static final String SENTENCIA_CREAR_TABLA =
            "CREATE TABLE " + TABLA_PRODUCTO + " (" +
                    COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMNA_NOMBRE + " TEXT NOT NULL, " +
                    COLUMNA_CATEGORIA + " TEXT NOT NULL, " +
                    COLUMNA_UNIDAD_MEDIDA + " TEXT NOT NULL, " +
                    COLUMNA_STOCK_ACTUAL + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMNA_STOCK_MINIMO + " INTEGER NOT NULL DEFAULT 0);";

    public ProductoDBHelper(Context context) {
        super(context, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase baseDatos) {
        baseDatos.execSQL(SENTENCIA_CREAR_TABLA);
        insertarDatosDePrueba(baseDatos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase baseDatos, int versionAnterior, int versionNueva) {
        baseDatos.execSQL("DROP TABLE IF EXISTS " + TABLA_PRODUCTO);
        onCreate(baseDatos);
    }

    /**
     * Inserta algunos productos de ejemplo la primera vez que se crea
     * la base de datos, para que la aplicación no arranque vacía.
     */
    private void insertarDatosDePrueba(SQLiteDatabase baseDatos) {
        baseDatos.execSQL("INSERT INTO " + TABLA_PRODUCTO +
                " (" + COLUMNA_NOMBRE + ", " + COLUMNA_CATEGORIA + ", " + COLUMNA_UNIDAD_MEDIDA +
                ", " + COLUMNA_STOCK_ACTUAL + ", " + COLUMNA_STOCK_MINIMO + ") VALUES " +
                "('Hielo raspado', 'Insumo', 'Kilogramo', 50, 10)," +
                "('Leche condensada', 'Insumo', 'Unidad', 30, 8)," +
                "('Vaso cholado grande', 'Empaque', 'Unidad', 120, 30);");
    }
}
