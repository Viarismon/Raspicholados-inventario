package co.sena.raspicholados.api;

import co.sena.raspicholados.api.config.Configuracion;
import co.sena.raspicholados.api.controlador.AutenticacionControlador;
import co.sena.raspicholados.api.controlador.CategoriaControlador;
import co.sena.raspicholados.api.controlador.MovimientoControlador;
import co.sena.raspicholados.api.controlador.ProductoControlador;
import co.sena.raspicholados.api.controlador.ProveedorControlador;
import co.sena.raspicholados.api.controlador.ReporteControlador;
import co.sena.raspicholados.api.controlador.SistemaControlador;
import co.sena.raspicholados.api.http.ManejadorEstatico;
import co.sena.raspicholados.api.http.Router;
import co.sena.raspicholados.api.repositorio.CategoriaRepositorio;
import co.sena.raspicholados.api.repositorio.MovimientoRepositorio;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.repositorio.ProveedorRepositorio;
import co.sena.raspicholados.api.repositorio.UsuarioRepositorio;
import co.sena.raspicholados.api.repositorio.memoria.BaseDatosMemoria;
import co.sena.raspicholados.api.repositorio.memoria.CategoriaRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.MovimientoRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.ProductoRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.ProveedorRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.memoria.UsuarioRepositorioMemoria;
import co.sena.raspicholados.api.repositorio.mysql.CategoriaRepositorioMySQL;
import co.sena.raspicholados.api.repositorio.mysql.ConexionMySQL;
import co.sena.raspicholados.api.repositorio.mysql.MovimientoRepositorioMySQL;
import co.sena.raspicholados.api.repositorio.mysql.ProductoRepositorioMySQL;
import co.sena.raspicholados.api.repositorio.mysql.ProveedorRepositorioMySQL;
import co.sena.raspicholados.api.repositorio.mysql.UsuarioRepositorioMySQL;
import co.sena.raspicholados.api.seguridad.JwtUtil;
import co.sena.raspicholados.api.servicio.AutenticacionServicio;
import co.sena.raspicholados.api.servicio.CategoriaServicio;
import co.sena.raspicholados.api.servicio.MovimientoServicio;
import co.sena.raspicholados.api.servicio.ProductoServicio;
import co.sena.raspicholados.api.servicio.ProveedorServicio;
import co.sena.raspicholados.api.servicio.ReporteServicio;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;

/**
 * Punto de entrada de la aplicación web de inventario de Raspicholados.
 *   - GA7-220501096-AA5-EV03: API REST (28 servicios).
 *   - GA8-220501096-AA1-EV01: integración con el front-end, que ahora se
 *     sirve desde la carpeta web/ en el mismo servidor.
 *
 * Arquitectura en capas (de afuera hacia adentro):
 *   http (Router)  ->  controlador  ->  servicio (reglas de negocio)
 *                  ->  repositorio (interfaz)  ->  memoria | mysql
 *
 * Main ensambla las piezas (inyección de dependencias manual): elige la
 * implementación de persistencia según config.properties, crea los
 * servicios y registra las rutas de cada controlador.
 */
public class Main {

    public static final String VERSION = "1.1.0";

    public static void main(String[] args) throws Exception {
        Configuracion config = new Configuracion(args.length > 0 ? args[0] : "config.properties");
        int puerto = config.entero("servidor.puerto", 8080);

        // --- Seguridad: secreto del JWT ---------------------------------------
        String secreto = config.texto("jwt.secreto", null);
        if (secreto == null || secreto.length() < 32) {
            byte[] aleatorio = new byte[48];
            new SecureRandom().nextBytes(aleatorio);
            secreto = Base64.getEncoder().encodeToString(aleatorio);
            System.out.println("[AVISO] jwt.secreto no está configurado (o tiene menos de 32 caracteres).");
            System.out.println("        Se generó uno aleatorio: los tokens dejarán de servir al reiniciar.");
        }
        JwtUtil jwt = new JwtUtil(secreto, config.entero("jwt.minutos.vigencia", 120));

        // --- Persistencia ------------------------------------------------------
        String modo = config.texto("persistencia.modo", "memoria").toLowerCase();
        UsuarioRepositorio usuarios;
        CategoriaRepositorio categorias;
        ProveedorRepositorio proveedores;
        ProductoRepositorio productos;
        MovimientoRepositorio movimientos;

        ConexionMySQL conexion = null;
        if ("mysql".equals(modo)) {
            conexion = new ConexionMySQL(
                    config.texto("bd.url", "jdbc:mysql://localhost:3306/raspicholados_db?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true"),
                    config.texto("bd.usuario", "root"),
                    config.textoPermitiendoVacio("bd.contrasena", ""));
            try {
                conexion.probar();
                System.out.println("[BD] Conexión con MySQL verificada.");
            } catch (Exception e) {
                System.err.println("[BD] No fue posible conectar con MySQL: " + e.getMessage());
                if (!config.logico("persistencia.respaldo.memoria", true)) {
                    System.err.println("[BD] El servidor no puede iniciar sin base de datos.");
                    System.exit(1);
                }
                System.err.println("[BD] Se continúa en modo MEMORIA (datos de ejemplo, no persistentes).");
                conexion = null;
                modo = "memoria (respaldo: MySQL no disponible)";
            }
        }
        if (conexion != null) {
            usuarios = new UsuarioRepositorioMySQL(conexion);
            categorias = new CategoriaRepositorioMySQL(conexion);
            proveedores = new ProveedorRepositorioMySQL(conexion);
            productos = new ProductoRepositorioMySQL(conexion);
            movimientos = new MovimientoRepositorioMySQL(conexion);
        } else {
            BaseDatosMemoria bd = new BaseDatosMemoria();
            usuarios = new UsuarioRepositorioMemoria(bd);
            categorias = new CategoriaRepositorioMemoria(bd);
            proveedores = new ProveedorRepositorioMemoria(bd);
            productos = new ProductoRepositorioMemoria(bd);
            movimientos = new MovimientoRepositorioMemoria(bd);
            if (!modo.startsWith("memoria")) {
                modo = "memoria";
            }
        }

        // --- Servicios (lógica de negocio) ---------------------------------------
        AutenticacionServicio autenticacion = new AutenticacionServicio(usuarios, jwt);
        MovimientoServicio servicioMovimientos = new MovimientoServicio(movimientos, productos);
        ProductoServicio servicioProductos = new ProductoServicio(productos, categorias, proveedores, servicioMovimientos);

        String adminUsuario = config.texto("admin.usuario", "admin");
        String adminClave = config.texto("admin.contrasena.inicial", "Raspi2026*");
        if (autenticacion.crearAdministradorInicial(adminUsuario, adminClave)) {
            System.out.println("[SEGURIDAD] Se creó el administrador inicial '" + adminUsuario
                    + "'. Cambie su contraseña con PUT /api/auth/contrasena.");
        }

        // --- Rutas -------------------------------------------------------------------
        Router router = new Router(jwt);
        router.setOrigenCors(config.texto("cors.origen", null));
        new SistemaControlador(modo).registrarRutas(router);
        new AutenticacionControlador(autenticacion).registrarRutas(router);
        new CategoriaControlador(new CategoriaServicio(categorias)).registrarRutas(router);
        new ProveedorControlador(new ProveedorServicio(proveedores)).registrarRutas(router);
        new ProductoControlador(servicioProductos).registrarRutas(router);
        new MovimientoControlador(servicioMovimientos).registrarRutas(router);
        new ReporteControlador(new ReporteServicio(productos, movimientos)).registrarRutas(router);

        File carpetaWeb = new File(config.texto("web.directorio", "web"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/api", router);                         // servicios REST
        servidor.createContext("/", new ManejadorEstatico(carpetaWeb)); // aplicación web
        servidor.setExecutor(Executors.newFixedThreadPool(16));
        servidor.start();

        System.out.println("==============================================================");
        System.out.println(" Inventario RASPICHOLADOS v" + VERSION + " (aplicación web + API REST)");
        System.out.println(" Aplicación   : http://localhost:" + puerto + "/");
        System.out.println(" URL base API : http://localhost:" + puerto + "/api");
        System.out.println(" Persistencia : " + modo);
        System.out.println(" Rutas        : " + router.getRutas().size() + " (ver GET /api)");
        System.out.println(" Documentación: GET /api/docs  y  docs/API.md");
        if (!new File(carpetaWeb, "index.html").isFile()) {
            System.out.println(" [AVISO] No se encontró " + carpetaWeb.getPath() + File.separator + "index.html");
        }
        System.out.println(" Detener      : Ctrl + C");
        System.out.println("==============================================================");
    }
}
