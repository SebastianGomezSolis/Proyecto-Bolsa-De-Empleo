package una.sistema.proyectobolsaempleobackend.logic.model;

import lombok.Getter;
import lombok.Setter;

// Clase que representa el resultado del matching (emparejamiento) entre un puesto
// y un oferente. Contiene informacion sobre la similitud entre las habilidades
// del candidato y los requisitos del puesto para ranking y presentacion.
@Getter
@Setter
public class CandidatoResultado {
    private Oferente oferente;
    private double similitud;
    private double porcentaje;
    private int requisitosCumplidos;
    private int totalRequisitos;
}
