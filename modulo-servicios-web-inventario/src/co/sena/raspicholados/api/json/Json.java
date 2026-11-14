package co.sena.raspicholados.api.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Módulo: Servicios web de inventario
 * Propósito: Analizador (parser) y serializador JSON completo, escrito sin
 *            dependencias externas, conforme a la especificación RFC 8259.
 *
 * Esta clase reemplaza al JsonUtil de la evidencia AA5-EV01, que cortaba
 * las cadenas en la primera comilla escapada (hallazgo H-01 del informe
 * de pruebas AA5-EV02). Aquí se implementa un analizador descendente
 * recursivo que interpreta correctamente las secuencias de escape
 * (\" \\ \/ \b \f \n \r \t \\uXXXX), los números, los valores lógicos,
 * null, los objetos anidados y los arreglos.
 *
 * Tipos Java producidos al analizar:
 *   objeto  -> LinkedHashMap&lt;String, Object&gt;
 *   arreglo -> ArrayList&lt;Object&gt;
 *   cadena  -> String
 *   número  -> BigDecimal (se evita la pérdida de precisión de double)
 *   lógico  -> Boolean
 *   null    -> null
 *
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-11-14
 */
public final class Json {

    private final String texto;
    private int posicion;

    private Json(String texto) {
        this.texto = texto;
        this.posicion = 0;
    }

    // ------------------------------------------------------------------
    // Análisis (texto JSON -> objetos Java)
    // ------------------------------------------------------------------

    /**
     * Convierte un texto JSON en su representación Java.
     *
     * @param texto contenido JSON
     * @return Map, List, String, BigDecimal, Boolean o null
     * @throws JsonException si el texto no es JSON válido
     */
    public static Object analizar(String texto) {
        if (texto == null) {
            throw new JsonException("El contenido es nulo", 0);
        }
        Json analizador = new Json(texto);
        analizador.saltarEspacios();
        Object valor = analizador.leerValor();
        analizador.saltarEspacios();
        if (analizador.posicion < texto.length()) {
            throw new JsonException("Hay caracteres sobrantes después del valor JSON", analizador.posicion);
        }
        return valor;
    }

    private Object leerValor() {
        if (posicion >= texto.length()) {
            throw new JsonException("Fin inesperado del contenido", posicion);
        }
        char c = texto.charAt(posicion);
        switch (c) {
            case '{':
                return leerObjeto();
            case '[':
                return leerArreglo();
            case '"':
                return leerCadena();
            case 't':
                esperarPalabra("true");
                return Boolean.TRUE;
            case 'f':
                esperarPalabra("false");
                return Boolean.FALSE;
            case 'n':
                esperarPalabra("null");
                return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return leerNumero();
                }
                throw new JsonException("Carácter inesperado '" + c + "'", posicion);
        }
    }

    private Map<String, Object> leerObjeto() {
        Map<String, Object> objeto = new LinkedHashMap<String, Object>();
        posicion++; // '{'
        saltarEspacios();
        if (siguienteEs('}')) {
            posicion++;
            return objeto;
        }
        while (true) {
            saltarEspacios();
            if (!siguienteEs('"')) {
                throw new JsonException("Se esperaba el nombre de una propiedad entre comillas", posicion);
            }
            String clave = leerCadena();
            saltarEspacios();
            if (!siguienteEs(':')) {
                throw new JsonException("Se esperaba ':' después de la propiedad \"" + clave + "\"", posicion);
            }
            posicion++;
            saltarEspacios();
            objeto.put(clave, leerValor());
            saltarEspacios();
            if (siguienteEs(',')) {
                posicion++;
                continue;
            }
            if (siguienteEs('}')) {
                posicion++;
                return objeto;
            }
            throw new JsonException("Se esperaba ',' o '}' dentro del objeto", posicion);
        }
    }

    private List<Object> leerArreglo() {
        List<Object> arreglo = new ArrayList<Object>();
        posicion++; // '['
        saltarEspacios();
        if (siguienteEs(']')) {
            posicion++;
            return arreglo;
        }
        while (true) {
            saltarEspacios();
            arreglo.add(leerValor());
            saltarEspacios();
            if (siguienteEs(',')) {
                posicion++;
                continue;
            }
            if (siguienteEs(']')) {
                posicion++;
                return arreglo;
            }
            throw new JsonException("Se esperaba ',' o ']' dentro del arreglo", posicion);
        }
    }

    private String leerCadena() {
        posicion++; // comilla de apertura
        StringBuilder resultado = new StringBuilder();
        while (posicion < texto.length()) {
            char c = texto.charAt(posicion++);
            if (c == '"') {
                return resultado.toString();
            }
            if (c == '\\') {
                if (posicion >= texto.length()) {
                    break;
                }
                char escape = texto.charAt(posicion++);
                switch (escape) {
                    case '"': resultado.append('"'); break;
                    case '\\': resultado.append('\\'); break;
                    case '/': resultado.append('/'); break;
                    case 'b': resultado.append('\b'); break;
                    case 'f': resultado.append('\f'); break;
                    case 'n': resultado.append('\n'); break;
                    case 'r': resultado.append('\r'); break;
                    case 't': resultado.append('\t'); break;
                    case 'u':
                        if (posicion + 4 > texto.length()) {
                            throw new JsonException("Secuencia \\u incompleta", posicion);
                        }
                        String hex = texto.substring(posicion, posicion + 4);
                        try {
                            resultado.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw new JsonException("Secuencia \\u inválida: " + hex, posicion);
                        }
                        posicion += 4;
                        break;
                    default:
                        throw new JsonException("Secuencia de escape inválida '\\" + escape + "'", posicion - 1);
                }
            } else if (c < 0x20) {
                throw new JsonException("Carácter de control sin escapar dentro de una cadena", posicion - 1);
            } else {
                resultado.append(c);
            }
        }
        throw new JsonException("Cadena sin comilla de cierre", posicion);
    }

    private BigDecimal leerNumero() {
        int inicio = posicion;
        if (siguienteEs('-')) {
            posicion++;
        }
        if (siguienteEs('0')) {
            posicion++;
        } else if (esDigito()) {
            while (esDigito()) {
                posicion++;
            }
        } else {
            throw new JsonException("Número inválido", posicion);
        }
        if (siguienteEs('.')) {
            posicion++;
            if (!esDigito()) {
                throw new JsonException("Se esperaban dígitos después del punto decimal", posicion);
            }
            while (esDigito()) {
                posicion++;
            }
        }
        if (siguienteEs('e') || siguienteEs('E')) {
            posicion++;
            if (siguienteEs('+') || siguienteEs('-')) {
                posicion++;
            }
            if (!esDigito()) {
                throw new JsonException("Exponente inválido", posicion);
            }
            while (esDigito()) {
                posicion++;
            }
        }
        return new BigDecimal(texto.substring(inicio, posicion));
    }

    private void esperarPalabra(String palabra) {
        if (!texto.startsWith(palabra, posicion)) {
            throw new JsonException("Valor no reconocido; se esperaba " + palabra, posicion);
        }
        posicion += palabra.length();
    }

    private boolean siguienteEs(char c) {
        return posicion < texto.length() && texto.charAt(posicion) == c;
    }

    private boolean esDigito() {
        return posicion < texto.length() && Character.isDigit(texto.charAt(posicion))
                && texto.charAt(posicion) < 128;
    }

    private void saltarEspacios() {
        while (posicion < texto.length()) {
            char c = texto.charAt(posicion);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                posicion++;
            } else {
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // Serialización (objetos Java -> texto JSON)
    // ------------------------------------------------------------------

    /**
     * Convierte un valor Java en texto JSON con sangría de 2 espacios,
     * para que las respuestas sean legibles en Postman y en el navegador.
     */
    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribirValor(sb, valor, 0);
        return sb.toString();
    }

    /** Convierte un valor Java en texto JSON sin espacios ni saltos (se usa en el JWT). */
    public static String escribirCompacto(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribirValor(sb, valor, -1);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escribirValor(StringBuilder sb, Object valor, int nivel) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof ConvertibleJson) {
            escribirValor(sb, ((ConvertibleJson) valor).aMapa(), nivel);
        } else if (valor instanceof String) {
            escribirCadena(sb, (String) valor);
        } else if (valor instanceof BigDecimal) {
            sb.append(((BigDecimal) valor).toPlainString());
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor.toString());
        } else if (valor instanceof Map) {
            Map<String, Object> mapa = (Map<String, Object>) valor;
            if (mapa.isEmpty()) {
                sb.append("{}");
                return;
            }
            boolean compacto = nivel < 0;
            sb.append(compacto ? "{" : "{\n");
            int i = 0;
            for (Map.Entry<String, Object> entrada : mapa.entrySet()) {
                sangria(sb, nivel + 1);
                escribirCadena(sb, entrada.getKey());
                sb.append(compacto ? ":" : ": ");
                escribirValor(sb, entrada.getValue(), compacto ? -1 : nivel + 1);
                if (++i < mapa.size()) {
                    sb.append(',');
                }
                if (!compacto) {
                    sb.append('\n');
                }
            }
            sangria(sb, nivel);
            sb.append('}');
        } else if (valor instanceof Collection) {
            Collection<Object> lista = (Collection<Object>) valor;
            if (lista.isEmpty()) {
                sb.append("[]");
                return;
            }
            boolean compacto = nivel < 0;
            sb.append(compacto ? "[" : "[\n");
            int i = 0;
            for (Object elemento : lista) {
                sangria(sb, nivel + 1);
                escribirValor(sb, elemento, compacto ? -1 : nivel + 1);
                if (++i < lista.size()) {
                    sb.append(',');
                }
                if (!compacto) {
                    sb.append('\n');
                }
            }
            sangria(sb, nivel);
            sb.append(']');
        } else {
            escribirCadena(sb, valor.toString());
        }
    }

    private static void escribirCadena(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void sangria(StringBuilder sb, int nivel) {
        // Con nivel negativo (modo compacto) el ciclo no agrega espacios.
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
    }
}
