package co.sena.raspicholados.inventario.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.sena.raspicholados.inventario.R;
import co.sena.raspicholados.inventario.modelo.Producto;

import java.util.List;

/**
 * Módulo: Inventario (versión móvil)
 * Propósito: Adaptador que enlaza la lista de productos con el
 *            RecyclerView de la pantalla principal, y expone un
 *            listener de clic para abrir la edición de cada producto.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-30
 */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    /**
     * Contrato que la actividad implementa para reaccionar cuando el
     * usuario toca un producto de la lista.
     */
    public interface OyenteClicProducto {
        void alHacerClicEnProducto(Producto producto);
    }

    private final List<Producto> listaProductos;
    private final OyenteClicProducto oyenteClic;

    public ProductoAdapter(List<Producto> listaProductos, OyenteClicProducto oyenteClic) {
        this.listaProductos = listaProductos;
        this.oyenteClic = oyenteClic;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup contenedorPadre, int tipoVista) {
        View vistaFila = LayoutInflater.from(contenedorPadre.getContext())
                .inflate(R.layout.item_producto, contenedorPadre, false);
        return new ProductoViewHolder(vistaFila);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder soporteVista, int posicion) {
        Producto productoActual = listaProductos.get(posicion);

        soporteVista.textoNombre.setText(productoActual.getNombreProducto());
        soporteVista.textoDetalle.setText(
                productoActual.getCategoria() + " · Stock: " +
                        productoActual.getStockActual() + " " + productoActual.getUnidadMedida()
        );

        if (productoActual.tieneStockBajo()) {
            soporteVista.textoEstado.setText(R.string.estado_stock_bajo);
            soporteVista.textoEstado.setTextColor(Color.parseColor("#B00020"));
        } else {
            soporteVista.textoEstado.setText(R.string.estado_stock_normal);
            soporteVista.textoEstado.setTextColor(Color.parseColor("#1B2A41"));
        }

        soporteVista.itemView.setOnClickListener(vista -> oyenteClic.alHacerClicEnProducto(productoActual));
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    /**
     * Contenedor de las vistas de una fila de la lista de productos.
     */
    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombre;
        TextView textoDetalle;
        TextView textoEstado;

        ProductoViewHolder(@NonNull View vistaFila) {
            super(vistaFila);
            textoNombre = vistaFila.findViewById(R.id.textoNombreProducto);
            textoDetalle = vistaFila.findViewById(R.id.textoDetalleProducto);
            textoEstado = vistaFila.findViewById(R.id.textoEstadoProducto);
        }
    }
}
