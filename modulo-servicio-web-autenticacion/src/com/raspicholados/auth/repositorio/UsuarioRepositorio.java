package com.raspicholados.auth.repositorio;

import com.raspicholados.auth.modelo.Usuario;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio responsable de almacenar y consultar los usuarios.
 *
 * Para esta evidencia se utiliza una estructura en memoria (ConcurrentHashMap,
 * segura para acceso concurrente de múltiples peticiones HTTP) con respaldo
 * en un archivo de texto plano (usuarios.txt), de manera que los registros
 * persistan entre reinicios del servicio sin necesidad de un motor de base
 * de datos externo. Esta decisión responde al principio de "simplicidad
 * apropiada" (Pressman y Maxim, 2020): la complejidad de una base de datos
 * relacional no está justificada para un servicio de autenticación aislado
 * y de alcance formativo como el solicitado en esta evidencia.
 *
 * Si el proyecto evolucionara a producción, este repositorio podría
 * sustituirse por una implementación con JDBC + MySQL, tal como se hizo en
 * el módulo de inventario de Raspicholados (evidencias AA3-EV01/EV02),
 * sin afectar al resto de las capas (servicio y controladores web).
 */
public class UsuarioRepositorio {

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    private final File archivoPersistencia = new File("usuarios.txt");

    public UsuarioRepositorio() {
        cargarDesdeArchivo();
    }

    /** Indica si ya existe un usuario registrado con ese nombre. */
    public boolean existe(String nombreUsuario) {
        return usuarios.containsKey(nombreUsuario);
    }

    /** Recupera un usuario por su nombre, o null si no existe. */
    public Usuario buscarPorNombre(String nombreUsuario) {
        return usuarios.get(nombreUsuario);
    }

    /** Guarda un nuevo usuario y lo persiste en el archivo de respaldo. */
    public void guardar(Usuario usuario) {
        usuarios.put(usuario.getNombreUsuario(), usuario);
        persistirEnArchivo(usuario);
    }

    // ------------------------------------------------------------------
    // Persistencia simple en archivo de texto (formato: usuario|hash|salt)
    // ------------------------------------------------------------------

    private void persistirEnArchivo(Usuario usuario) {
        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(archivoPersistencia, true))) {
            escritor.write(usuario.getNombreUsuario() + "|" +
                    usuario.getHashContrasena() + "|" + usuario.getSalt());
            escritor.newLine();
        } catch (IOException e) {
            // Se registra en consola; el registro en memoria ya quedó realizado,
            // por lo que la petición actual no debe fallar por un error de E/S.
            System.err.println("No se pudo persistir el usuario en archivo: " + e.getMessage());
        }
    }

    private void cargarDesdeArchivo() {
        if (!archivoPersistencia.exists()) {
            return;
        }
        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivoPersistencia))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length == 3) {
                    usuarios.put(partes[0], new Usuario(partes[0], partes[1], partes[2]));
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo cargar el archivo de usuarios: " + e.getMessage());
        }
    }
}
