package co.sena.raspicholados.inventario.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.sena.raspicholados.inventario.R;
import co.sena.raspicholados.inventario.adapter.ProductoAdapter;
import co.sena.raspicholados.inventario.dao.ProductoDAO;
import co.sena.raspicholados.inventario.modelo.Producto;

import java.util.List;

/**
 * Módulo: Inventario (versión móvil)
 * Propósito: Actividad principal. Muestra el listado de productos del
 *            inventario y permite navegar hacia el formulario para
 *            crear un producto nuevo o editar uno existente.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-30
 */
public class MainActivity extends AppCompatActivity implements ProductoAdapter.OyenteClicProducto {

    public static final String CLAVE_PRODUCTO_SELECCIONADO = "producto_seleccionado";

    private ProductoDAO productoDAO;
    private RecyclerView recyclerProductos;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_main);

        productoDAO = new ProductoDAO(this);

        recyclerProductos = findViewById(R.id.recyclerProductos);
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        View botonNuevoProducto = findViewById(R.id.botonNuevoProducto);
        botonNuevoProducto.setOnClickListener(vista -> {
            Intent intencion = new Intent(MainActivity.this, FormularioProductoActivity.class);
            startActivity(intencion);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se vuelve a consultar la lista cada vez que la pantalla
        // regresa a primer plano, para reflejar altas, ediciones o
        // eliminaciones hechas en el formulario.
        cargarListaProductos();
    }

    /**
     * Consulta todos los productos por medio del DAO y actualiza el
     * RecyclerView con el adaptador correspondiente.
     */
    private void cargarListaProductos() {
        List<Producto> listaProductos = productoDAO.listarProductos();
        ProductoAdapter adaptador = new ProductoAdapter(listaProductos, this);
        recyclerProductos.setAdapter(adaptador);
    }

    /**
     * Al tocar un producto de la lista, se abre el formulario en modo
     * edición, enviando el producto seleccionado por Intent.
     */
    @Override
    public void alHacerClicEnProducto(Producto producto) {
        Intent intencion = new Intent(MainActivity.this, FormularioProductoActivity.class);
        intencion.putExtra(CLAVE_PRODUCTO_SELECCIONADO, producto);
        startActivity(intencion);
    }
}
