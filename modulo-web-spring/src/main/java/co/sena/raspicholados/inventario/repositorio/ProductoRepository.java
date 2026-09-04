package co.sena.raspicholados.inventario.repositorio;

import co.sena.raspicholados.inventario.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Módulo: Inventario (versión web con framework)
 * Propósito: Repositorio Spring Data JPA para la entidad Producto.
 *            Al extender JpaRepository, el framework genera en
 *            tiempo de ejecución la implementación de las operaciones
 *            CRUD (guardar, consultar, listar, eliminar) sin
 *            necesidad de escribir sentencias SQL manualmente, a
 *            diferencia del DAO manual con JDBC de la evidencia EV01.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-09-02
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Consulta derivada: Spring Data genera automáticamente la
     * sentencia SQL a partir del nombre del método (busca productos
     * cuyo stock actual sea menor al stock mínimo, es decir, con
     * alerta de stock bajo, RF-04).
     */
    List<Producto> findByStockActualLessThan(int stockMinimo);
}
