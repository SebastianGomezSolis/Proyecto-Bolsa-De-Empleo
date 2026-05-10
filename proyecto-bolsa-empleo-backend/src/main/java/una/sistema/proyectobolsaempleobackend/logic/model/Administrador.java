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
    // Clave primaria autoincrementable para identificar unicamente al administrador
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacion uno a uno con la entidad Usuario.
    // Cada administrador esta vinculado a un unico usuario del sistema.
    // El campo usuario_id es la clave foranea y no puede ser nulo.
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Numero de identificacion unico del administrador (ej: CEDULA o ID institucional)
    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    // Nombre completo del administrador
    @Column(nullable = false, length = 100)
    private String nombre;
}
