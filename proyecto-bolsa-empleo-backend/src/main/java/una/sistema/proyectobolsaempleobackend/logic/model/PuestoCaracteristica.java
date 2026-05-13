package una.sistema.proyectobolsaempleobackend.logic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    @JsonIgnore
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    @Column(name = "nivel_requerido", nullable = false)
    private Integer nivelRequerido;

    @JsonProperty("nombre")
    public String getNombre() {
        return caracteristica != null ? caracteristica.getNombre() : null;
    }
}
