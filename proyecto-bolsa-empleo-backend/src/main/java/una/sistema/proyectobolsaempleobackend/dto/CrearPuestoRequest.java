package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// DTO para la creacion de un nuevo puesto de trabajo.
// Contiene la informacion del puesto y la lista de caracteristicas (con niveles)
// requeridas como requisitos para los candidatos.
@Getter
@Setter
public class CrearPuestoRequest {
    // Descripcion detallada del puesto (requisitos, responsabilidades, beneficios)
    private String descripcion;

    // Salario ofrecido para el puesto
    private BigDecimal salario;

    // Tipo de publicacion: "publico" (visible para todos) o "privado" (solo para oferentes)
    private String tipoPublicacion;

    // Lista de IDs de las caracteristicas (hojas) que son requisitos para el puesto
    private List<Integer> caracteristicaIds;

    // Mapa que relaciona cada ID de caracteristica con su nivel requerido.
    // Formato: "nivel_ID" -> "valor" (ej: "nivel_5" -> "3")
    private Map<String, String> niveles;
}
