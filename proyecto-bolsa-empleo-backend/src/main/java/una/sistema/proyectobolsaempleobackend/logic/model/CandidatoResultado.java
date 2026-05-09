package una.sistema.proyectobolsaempleobackend.logic.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidatoResultado {
    private Oferente oferente;
    private double similitud;
    private double porcentaje;
    private int requisitosCumplidos;
    private int totalRequisitos;
}
