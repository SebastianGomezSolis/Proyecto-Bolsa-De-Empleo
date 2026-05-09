package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CrearPuestoRequest {
    private String descripcion;
    private BigDecimal salario;
    private String tipoPublicacion;
    private List<Integer> caracteristicaIds;
    private Map<String, String> niveles;
}
