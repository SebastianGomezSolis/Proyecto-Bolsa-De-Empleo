package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oferente")
@Getter
@Setter
public class Oferente {
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

    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    @ManyToOne
    @JoinColumn(name = "nacionalidad", nullable = false)
    private Nacionalidad nacionalidad;

    @Column(length = 20)
    private String telefono;

    @Column(name = "lugar_residencia", length = 150)
    private String lugarResidencia;

    @Column(nullable = false)
    private Boolean autorizado = false;

    @Column(length = 255)
    private String curriculum;
}
