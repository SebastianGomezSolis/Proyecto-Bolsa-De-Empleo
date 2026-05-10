package una.sistema.proyectobolsaempleobackend.logic.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
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
    // Clave primaria unica de la caracteristica
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nombre de la caracteristica (ej: "Java", "Base de Datos", "Trabajo en equipo")
    @Column(nullable = false, length = 100)
    private String nombre;

    // Relacion muchos a uno con la misma entidad (autoreferencia).
    // Indica la caracteristica padre en la jerarquia.
    // Si es null, la caracteristica es una raiz (categoria de nivel superior).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    @JsonIgnore
    private Caracteristica padre;

    // Relacion uno a muchos con las subcaracteristicas (hijos).
    // Lista de caracteristicas que tienen a esta como padre.
    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Caracteristica> hijos;

    // Anotacion de Jackson para excluir de la serializacion JSON (evita recursion infinita)
    @JsonIgnore
    public boolean isHoja() {
        // Retorna true si la caracteristica no tiene hijos (es una hoja del arbol).
        // Solo las hojas pueden ser usadas como habilidades o requisitos.
        return hijos == null || hijos.isEmpty();
    }
}
