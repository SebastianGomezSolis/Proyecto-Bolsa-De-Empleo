package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa una habilidad registrada por un oferente.
// Cada habilidad vincula a un oferente con una caracteristica (hoja del arbol) y define un nivel de competencia (de 1 a 5).
@Entity
@Table(name = "habilidad",
       uniqueConstraints = @UniqueConstraint(columnNames = {"oferente_id", "caracteristica_id"}))
@Getter
@Setter
public class Habilidad {
    // Clave primaria unica de la habilidad
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacion muchos a uno con la entidad Oferente.
    // Indica a que oferente pertenece esta habilidad.
    @ManyToOne
    @JoinColumn(name = "oferente_id", nullable = false)
    private Oferente oferente;

    // Relacion muchos a uno con la entidad Caracteristica (debe ser hoja).
    // Indica la habilidad o competencia especifica.
    @ManyToOne
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    // Nivel de dominio de la habilidad, en una escala de 1 a 5.
    // 1 = Basico, 3 = Intermedio, 5 = Experto
    @Column(nullable = false)
    private Integer nivel;
}
