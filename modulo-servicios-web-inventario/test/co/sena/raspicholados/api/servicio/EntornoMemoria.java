package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.repositorio.memoria.BaseDatosMemoria;
import co.sena.raspicholados.api.repositorio.memoria.CategoriaRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.MovimientoRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.ProductoRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.ProveedorRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.UsuarioRepositorioMemoria;

/**
 * Arma los servicios sobre repositorios en memoria. Gracias a que los
 * servicios dependen de interfaces (inversión de dependencias), las pruebas
 * unitarias no necesitan MySQL: cada prueba recibe una base nueva con los
 * datos de ejemplo (5 categorías, 4 proveedores, 9 productos).
 */
class EntornoMemoria {

    final BaseDatosMemoria bd = new BaseDatosMemoria();
    final UsuarioRepositorioMemoria usuarios = new UsuarioRepositorioMemoria(bd);
    final CategoriaRepositorioMemoria categorias = new CategoriaRepositorioMemoria(bd);
    final ProveedorRepositorioMemoria proveedores = new ProveedorRepositorioMemoria(bd);
    final ProductoRepositorioMemoria productos = new ProductoRepositorioMemoria(bd);
    final MovimientoRepositorioMemoria movimientos = new MovimientoRepositorioMemoria(bd);

    final MovimientoServicio servicioMovimientos = new MovimientoServicio(movimientos, productos);
    final ProductoServicio servicioProductos = new ProductoServicio(productos, categorias, proveedores, servicioMovimientos);
    final CategoriaServicio servicioCategorias = new CategoriaServicio(categorias);
    final ReporteServicio servicioReportes = new ReporteServicio(productos, movimientos);
}
