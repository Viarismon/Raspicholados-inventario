package com.raspicholados.auth;

import com.raspicholados.auth.repositorio.UsuarioRepositorio;
import com.raspicholados.auth.servicio.AutenticacionServicio;
import com.raspicholados.auth.web.LoginHandler;
import com.raspicholados.auth.web.RegistroHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Punto de entrada del servicio web de autenticación de Raspicholados
 * (evidencia GA7-220501096-AA5-EV01).
 *
 * El servicio expone dos operaciones REST:
 *   POST /api/registro -> registra un nuevo usuario
 *   POST /api/login    -> valida usuario y contraseña
 *
 * Se implementa con com.sun.net.httpserver.HttpServer, incluido de forma
 * nativa en el JDK, para evitar dependencias externas (no se requiere
 * Maven/Gradle ni un contenedor de servlets como Tomcat) y facilitar así
 * la compilación y ejecución del proyecto en cualquier equipo con Java
 * instalado, algo especialmente relevante para la sustentación de la
 * evidencia ante el instructor.
 */
public class Main {

    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {
        // Capas de la aplicación: repositorio (persistencia) -> servicio (negocio) -> web (presentación)
        UsuarioRepositorio repositorio = new UsuarioRepositorio();
        AutenticacionServicio servicio = new AutenticacionServicio(repositorio);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.createContext("/api/registro", new RegistroHandler(servicio));
        servidor.createContext("/api/login", new LoginHandler(servicio));

        // Pool de hilos por defecto (ejecutor nulo = hilo por petición).
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("========================================================");
        System.out.println(" Servicio web de autenticación RASPICHOLADOS iniciado");
        System.out.println(" Escuchando en: http://localhost:" + PUERTO);
        System.out.println(" Endpoints disponibles:");
        System.out.println("   POST /api/registro  { \"usuario\":\"...\", \"contrasena\":\"...\" }");
        System.out.println("   POST /api/login     { \"usuario\":\"...\", \"contrasena\":\"...\" }");
        System.out.println("========================================================");
    }
}
