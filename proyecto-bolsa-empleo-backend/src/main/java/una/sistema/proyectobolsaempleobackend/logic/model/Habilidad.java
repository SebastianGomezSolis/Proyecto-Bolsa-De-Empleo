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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "oferente_id", nullable = false)
    private Oferente oferente;

    @ManyToOne
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    @Column(nullable = false)
    private Integer nivel;
}
