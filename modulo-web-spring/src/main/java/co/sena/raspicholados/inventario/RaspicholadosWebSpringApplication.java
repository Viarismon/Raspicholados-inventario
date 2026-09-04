package co.sena.raspicholados.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Módulo: Inventario (versión web con framework)
 * Propósito: Punto de arranque de la aplicación Spring Boot. Al
 *            ejecutarse, el framework levanta automáticamente un
 *            servidor web embebido (Tomcat) y configura la conexión
 *            a la base de datos según application.properties.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-09-02
 */
@SpringBootApplication
public class RaspicholadosWebSpringApplication {

    public static void main(String[] argumentos) {
        SpringApplication.run(RaspicholadosWebSpringApplication.class, argumentos);
    }
}
