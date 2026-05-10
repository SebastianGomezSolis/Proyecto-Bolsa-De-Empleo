package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa a un administrador del sistema.
// Los administradores tienen la funcion de aprobar o rechazar
// registro de empresas y oferentes, y gestionar caracteristicas.
@Entity
@Table(name = "administrador")
@Getter
@Setter
public class Administrador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombre;
}
