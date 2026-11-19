/* ============================================================================
   RASPICHOLADOS · Capa de acceso a datos (api.js)
   ----------------------------------------------------------------------------
   Evidencia GA8-220501096-AA1-EV01 · Integración de módulos.

   Reemplaza al antiguo data.js (AA4-EV03), que guardaba todo en localStorage.
   Ahora cada operación es una petición HTTP a la API REST de la AA5-EV03,
   servida en el mismo origen (/api), con el token JWT en la cabecera
   Authorization.

   Patrones aplicados:
   - Fachada: app.js solo conoce ProductosAPI, MovimientosAPI, etc.; no sabe
     de fetch, cabeceras ni códigos HTTP.
   - Módulo: todo vive dentro de objetos con nombre; no se ensucia el ámbito
     global con variables sueltas.

   Este archivo NO manipula el DOM.
   ============================================================================ */

const URL_API = "/api";
const CLAVE_SESION = "raspicholados_sesion";

/** Error de la API con el código HTTP y el cuerpo de error estándar. */
class ErrorApi extends Error {
  constructor(estado, codigo, mensaje, detalles) {
    super(mensaje);
    this.estado = estado;
    this.codigo = codigo;
    this.detalles = detalles || [];
  }

  /** Texto para mostrar al usuario, incluyendo los errores por campo. */
  textoCompleto() {
    if (!this.detalles.length) return this.message;
    return this.message + ": " + this.detalles.map((d) => `${d.campo} (${d.mensaje})`).join("; ");
  }
}

/* ----------------------------------------------------------------------- */
/* Sesión: el token se guarda en sessionStorage (se borra al cerrar la     */
/* pestaña), nunca en localStorage.                                        */
/* ----------------------------------------------------------------------- */

const Sesion = {
  guardar(datosLogin) {
    const sesion = {
      token: datosLogin.token,
      usuario: datosLogin.usuario,
      expiraEn: Date.now() + datosLogin.expiraEnSegundos * 1000,
    };
    sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    return sesion;
  },

  obtener() {
    try {
      const sesion = JSON.parse(sessionStorage.getItem(CLAVE_SESION));
      if (!sesion || !sesion.token || Date.now() >= sesion.expiraEn) return null;
      return sesion;
    } catch (e) {
      return null;
    }
  },

  cerrar() {
    sessionStorage.removeItem(CLAVE_SESION);
  },

  esAdministrador() {
    const sesion = this.obtener();
    return !!sesion && sesion.usuario.rol === "ADMINISTRADOR";
  },
};

/** Se ejecuta cuando la API responde 401 (token vencido o inválido). */
let alExpirarSesion = () => {};
function configurarAlExpirarSesion(funcion) {
  alExpirarSesion = funcion;
}

/**
 * Cliente HTTP único: agrega el token, envía JSON y convierte las respuestas
 * de error en ErrorApi. Todas las demás funciones pasan por aquí.
 */
async function peticion(metodo, ruta, cuerpo) {
  const cabeceras = { Accept: "application/json" };
  const sesion = Sesion.obtener();
  if (sesion) cabeceras.Authorization = "Bearer " + sesion.token;
  if (cuerpo !== undefined) cabeceras["Content-Type"] = "application/json";

  let respuesta;
  try {
    respuesta = await fetch(URL_API + ruta, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    });
  } catch (e) {
    throw new ErrorApi(0, "SIN_CONEXION", "No hay conexión con el servidor. Verifique que run.bat esté abierto");
  }

  const texto = await respuesta.text();
  const json = texto ? JSON.parse(texto) : {};

  if (!respuesta.ok) {
    const error = json.error || {};
    if (respuesta.status === 401 && ruta !== "/auth/login") {
      Sesion.cerrar();
      alExpirarSesion();
    }
    throw new ErrorApi(respuesta.status, error.codigo || "ERROR", error.mensaje || "Error inesperado", error.detalles);
  }
  return json;
}

/* ----------------------------------------------------------------------- */
/* Fachadas por recurso                                                    */
/* ----------------------------------------------------------------------- */

const AutenticacionAPI = {
  async iniciarSesion(usuario, contrasena) {
    const json = await peticion("POST", "/auth/login", { usuario, contrasena });
    return Sesion.guardar(json.datos);
  },
  cerrarSesion() {
    Sesion.cerrar();
  },
};

const ProductosAPI = {
  async listar() {
    return (await peticion("GET", "/productos")).datos;
  },
  async obtener(id) {
    return (await peticion("GET", `/productos/${encodeURIComponent(id)}`)).datos;
  },
  async crear(datos) {
    return (await peticion("POST", "/productos", datos)).datos;
  },
  async actualizar(id, datos) {
    return (await peticion("PUT", `/productos/${encodeURIComponent(id)}`, datos)).datos;
  },
  /** La API no borra físicamente: marca el producto como inactivo (baja). */
  async darDeBaja(id) {
    return peticion("DELETE", `/productos/${encodeURIComponent(id)}`);
  },
};

const CategoriasAPI = {
  async listar() {
    return (await peticion("GET", "/categorias")).datos;
  },
};

const ProveedoresAPI = {
  async listar() {
    return (await peticion("GET", "/proveedores")).datos;
  },
};

const MovimientosAPI = {
  async listar(limite = 25) {
    return (await peticion("GET", `/movimientos?limite=${limite}`)).datos;
  },
  /** tipo: "ENTRADA" o "SALIDA". La API valida el stock disponible. */
  async registrar({ productoId, tipo, cantidad, motivo }) {
    return (await peticion("POST", "/movimientos", { productoId, tipo, cantidad, motivo })).datos;
  },
};

const ReportesAPI = {
  async alertas() {
    return (await peticion("GET", "/alertas/stock-bajo")).datos;
  },
  async resumen() {
    return (await peticion("GET", "/reportes/resumen")).datos;
  },
};

const UsuariosAPI = {
  async listar() {
    return (await peticion("GET", "/usuarios")).datos;
  },
  async registrar(datos) {
    return (await peticion("POST", "/usuarios", datos)).datos;
  },
};
