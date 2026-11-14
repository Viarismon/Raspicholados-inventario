package co.sena.raspicholados.api.http;

/**
 * Interfaz funcional que implementa cada operación de un controlador.
 * Permite registrar rutas con referencias a métodos, por ejemplo:
 * {@code router.get("/api/productos", Seguridad.AUTENTICADA, this::listar);}
 */
public interface Manejador {

    Respuesta manejar(Solicitud solicitud) throws Exception;
}
