package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.logic.model.TipoCambio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Servicio para obtener el tipo de cambio del dolar desde la API del
// Ministerio de Hacienda de Costa Rica.
// Se usa para convertir salarios de dolares a colones.
@Service
public class TipoCambioServicio {
    // URL de la API del Ministerio de Hacienda de Costa Rica para tipo de cambio
    private static final String API_URL = "https://api.hacienda.go.cr/indicadores/tc/dolar";

    // Obtiene el tipo de cambio actual del dolar.
    // Realiza una peticion GET a la API y parsea la respuesta JSON.
    // Retorna un objeto TipoCambio con valores de compra y venta, o null si falla.
    public TipoCambio obtenerTipoCambio() {
        try {
            // Crear conexion HTTP a la URL de la API
            URL url = new URL(API_URL);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            // Leer la respuesta de la API linea por linea
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conexion.getInputStream())
            );

            // Construir el JSON completo leyendo todas las lineas
            StringBuilder respuesta = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                respuesta.append(linea);
            }
            reader.close();

            // Parsear el JSON de respuesta
            JSONObject json = new JSONObject(respuesta.toString());

            // Extraer valores de compra y venta del JSON
            double compra = json.getJSONObject("compra").getDouble("valor");
            double venta  = json.getJSONObject("venta").getDouble("valor");

            // Crear y retornar el objeto TipoCambio
            return new TipoCambio(compra, venta, "USD");

        } catch (Exception e) {
            // Si ocurre cualquier error, imprimir stack trace y retornar null
            e.printStackTrace();
            return null;
        }
    }
}
