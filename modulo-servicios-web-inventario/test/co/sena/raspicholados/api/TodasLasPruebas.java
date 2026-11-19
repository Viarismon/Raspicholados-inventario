package co.sena.raspicholados.api;

import co.sena.raspicholados.api.config.ConfiguracionTest;
import co.sena.raspicholados.api.http.ManejadorEstaticoTest;
import co.sena.raspicholados.api.json.JsonTest;
import co.sena.raspicholados.api.modelo.ProductoTest;
import co.sena.raspicholados.api.seguridad.ContrasenaUtilTest;
import co.sena.raspicholados.api.seguridad.ControlIntentosTest;
import co.sena.raspicholados.api.seguridad.JwtUtilTest;
import co.sena.raspicholados.api.servicio.AutenticacionServicioTest;
import co.sena.raspicholados.api.servicio.MovimientoServicioTest;
import co.sena.raspicholados.api.servicio.ProductoServicioTest;
import co.sena.raspicholados.api.servicio.ReporteYCategoriaServicioTest;
import co.sena.raspicholados.api.servicio.ValidadorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite con todas las pruebas unitarias de la aplicación, agrupadas por
 * módulo (configuración, http, json, modelo, seguridad y servicios).
 * Se ejecuta con test.bat / test.sh o desde el IDE (clic derecho > Run).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConfiguracionTest.class,
        ManejadorEstaticoTest.class,
        JsonTest.class,
        ProductoTest.class,
        ContrasenaUtilTest.class,
        ControlIntentosTest.class,
        JwtUtilTest.class,
        AutenticacionServicioTest.class,
        MovimientoServicioTest.class,
        ProductoServicioTest.class,
        ReporteYCategoriaServicioTest.class,
        ValidadorTest.class
})
public class TodasLasPruebas {
}
