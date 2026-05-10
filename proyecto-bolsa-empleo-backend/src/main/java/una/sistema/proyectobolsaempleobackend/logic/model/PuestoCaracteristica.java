package una.sistema.proyectobolsaempleobackend.logic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad de asociacion que vincula un puesto con una caracteristica requerida.
// Define que nivel minimo de esa caracteristica debe tener un candidato para el puesto.
@Entity
@Table(name = "puesto_caracteristica")
@Getter
@Setter
public class PuestoCaracteristica {
    // Clave primaria unica del registro
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacion muchos a uno con la entidad Puesto.
    // Indica a que puesto pertenece este requisito.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    @JsonIgnore
    private Puesto puesto;

    // Relacion muchos a uno con la entidad Caracteristica (debe ser hoja).
    // Indica la competencia o habilidad requerida.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    // Nivel minimo requerido de la caracteristica para que un oferente sea considerado.
    // Valor entre 1 y 5.
    @Column(name = "nivel_requerido", nullable = false)
    private Integer nivelRequerido;
}
