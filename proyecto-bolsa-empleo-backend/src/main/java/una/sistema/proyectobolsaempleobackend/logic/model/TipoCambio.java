package una.sistema.proyectobolsaempleobackend.logic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Clase que representa el tipo de cambio del dolar.
// Se obtiene de la API del Ministerio de Hacienda de Costa Rica.
// Usado para convertir salarios de dolares a colones.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TipoCambio {
    // Valor de compra del dolar (cuanto te pagan por un dolar)
    private double compra;

    // Valor de venta del dolar (cuanto cuesta un dolar)
    private double venta;

    // Moneda de referencia (normalmente "USD" para dolares americanos)
    private String moneda;
}
