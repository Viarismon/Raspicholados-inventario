package co.sena.raspicholados.inventario.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import co.sena.raspicholados.inventario.R;
import co.sena.raspicholados.inventario.dao.ProductoDAO;
import co.sena.raspicholados.inventario.modelo.Producto;

/**
 * Módulo: Inventario (versión móvil)
 * Propósito: Actividad de formulario, usada tanto para registrar un
 *            producto nuevo como para editar o eliminar uno existente,
 *            según si llega un Producto por Intent desde MainActivity.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-30
 */
public class FormularioProductoActivity extends AppCompatActivity {

    private ProductoDAO productoDAO;
    private Producto productoEnEdicion;
    private boolean esModoEdicion;

    private EditText campoNombre;
    private EditText campoCategoria;
    private EditText campoUnidadMedida;
    private EditText campoStockActual;
    private EditText campoStockMinimo;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_formulario_producto);

        productoDAO = new ProductoDAO(this);

        campoNombre = findViewById(R.id.campoNombre);
        campoCategoria = findViewById(R.id.campoCategoria);
        campoUnidadMedida = findViewById(R.id.campoUnidadMedida);
        campoStockActual = findViewById(R.id.campoStockActual);
        campoStockMinimo = findViewById(R.id.campoStockMinimo);

        TextView tituloFormulario = findViewById(R.id.tituloFormulario);
        Button botonGuardar = findViewById(R.id.botonGuardar);
        Button botonEliminar = findViewById(R.id.botonEliminar);

        productoEnEdicion = (Producto) getIntent()
                .getSerializableExtra(MainActivity.CLAVE_PRODUCTO_SELECCIONADO);
        esModoEdicion = productoEnEdicion != null;

        if (esModoEdicion) {
            tituloFormulario.setText(R.string.titulo_editar_producto);
            botonEliminar.setVisibility(View.VISIBLE);
            precargarDatosDelProducto();
        } else {
            tituloFormulario.setText(R.string.titulo_nuevo_producto);
            botonEliminar.setVisibility(View.GONE);
        }

        botonGuardar.setOnClickListener(vista -> guardarProducto());
        botonEliminar.setOnClickListener(vista -> confirmarEliminacion());
    }

    /**
     * Cuando el formulario abre en modo edición, llena los campos con
     * los datos del producto recibido por Intent.
     */
    private void precargarDatosDelProducto() {
        campoNombre.setText(productoEnEdicion.getNombreProducto());
        campoCategoria.setText(productoEnEdicion.getCategoria());
        campoUnidadMedida.setText(productoEnEdicion.getUnidadMedida());
        campoStockActual.setText(String.valueOf(productoEnEdicion.getStockActual()));
        campoStockMinimo.setText(String.valueOf(productoEnEdicion.getStockMinimo()));
    }

    /**
     * Valida los campos del formulario y, según el modo, inserta un
     * producto nuevo o actualiza el producto en edición.
     */
    private void guardarProducto() {
        String nombreProducto = campoNombre.getText().toString().trim();
        String categoria = campoCategoria.getText().toString().trim();
        String unidadMedida = campoUnidadMedida.getText().toString().trim();
        String textoStockActual = campoStockActual.getText().toString().trim();
        String textoStockMinimo = campoStockMinimo.getText().toString().trim();

        if (nombreProducto.isEmpty() || categoria.isEmpty() || unidadMedida.isEmpty()
                || textoStockActual.isEmpty() || textoStockMinimo.isEmpty()) {
            Toast.makeText(this, R.string.mensaje_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }

        int stockActual = Integer.parseInt(textoStockActual);
        int stockMinimo = Integer.parseInt(textoStockMinimo);

        Producto producto = new Producto(nombreProducto, categoria, unidadMedida, stockActual, stockMinimo);

        boolean operacionExitosa;
        if (esModoEdicion) {
            producto.setIdProducto(productoEnEdicion.getIdProducto());
            operacionExitosa = productoDAO.actualizarProducto(producto);
        } else {
            operacionExitosa = productoDAO.insertarProducto(producto);
        }

        if (operacionExitosa) {
            Toast.makeText(this, R.string.mensaje_guardado_exitoso, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.mensaje_error_guardar, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar el
     * producto, para evitar borrados accidentales.
     */
    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_confirmar_eliminacion)
                .setMessage(R.string.mensaje_confirmar_eliminacion)
                .setPositiveButton(R.string.boton_eliminar, (dialogo, boton) -> {
                    boolean eliminado = productoDAO.eliminarProducto(productoEnEdicion.getIdProducto());
                    if (eliminado) {
                        Toast.makeText(this, R.string.mensaje_eliminado_exitoso, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.boton_cancelar, null)
                .show();
    }
}
