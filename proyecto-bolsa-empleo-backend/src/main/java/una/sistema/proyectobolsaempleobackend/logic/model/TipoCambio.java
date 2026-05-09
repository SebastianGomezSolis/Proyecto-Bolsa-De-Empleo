package una.sistema.proyectobolsaempleobackend.logic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TipoCambio {
    private double compra;
    private double venta;
    private String moneda;
}
