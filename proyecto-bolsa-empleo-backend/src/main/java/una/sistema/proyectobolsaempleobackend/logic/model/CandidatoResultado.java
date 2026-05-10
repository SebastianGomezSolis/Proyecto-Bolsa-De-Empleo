package una.sistema.proyectobolsaempleobackend.logic.model;

import lombok.Getter;
import lombok.Setter;

// Clase que representa el resultado del matching (emparejamiento) entre un puesto
// y un oferente. Contiene informacion sobre la similitud entre las habilidades
// del candidato y los requisitos del puesto para ranking y presentacion.
@Getter
@Setter
public class CandidatoResultado {
    // El oferente que corresponde a este resultado
    private Oferente oferente;

    // Similitud calculada usando coseno (valor entre 0.0 y 1.0)
    private double similitud;

    // Porcentaje de compatibilidad (similitud * 100), valor entre 0 y 100
    private double porcentaje;

    // Cantidad de requisitos (caracteristicas del puesto) que el oferente cumple
    private int requisitosCumplidos;

    // Total de requisitos (caracteristicas) que tiene el puesto
    private int totalRequisitos;
}
