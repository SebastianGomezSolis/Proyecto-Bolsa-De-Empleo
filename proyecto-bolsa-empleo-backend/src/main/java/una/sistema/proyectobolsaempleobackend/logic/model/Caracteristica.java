package una.sistema.proyectobolsaempleobackend.logic.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Entidad que representa una caracteristica dentro del sistema.
// Las caracteristicas forman un arbol jerarquico (categorias y subcategorias) y se usan para describir habilidades requeridas en puestos y habilidades de oferentes.
@Entity
@Table(name = "caracteristica")
@Getter
@Setter
public class Caracteristica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    @JsonIgnore
    private Caracteristica padre;

    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Caracteristica> hijos;

    @JsonProperty("padreId")
    public Integer getPadreId() {
        return padre != null ? padre.getId() : null;
    }

    @JsonIgnore
    public boolean isHoja() {
        return hijos == null || hijos.isEmpty();
    }
}
