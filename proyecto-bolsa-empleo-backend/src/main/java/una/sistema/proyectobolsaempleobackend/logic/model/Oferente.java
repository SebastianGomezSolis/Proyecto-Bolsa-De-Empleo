package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa a un oferente.
// Los oferentes son personas que buscan empleo y registran sus habilidades en el sistema.
@Entity
@Table(name = "oferente")
@Getter
@Setter
public class Oferente {
    // Clave primaria unica del oferente, generada automaticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacion uno a uno con la entidad Usuario para credenciales de acceso
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Numero de identificacion unico del oferente (cedula, pasaporte, etc.)
    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    // Nombre(s) completo(s) del oferente
    @Column(nullable = false, length = 100)
    private String nombre;

    // Primer apellido del oferente
    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    // Relacion muchos a uno con la entidad Nacionalidad.
    // Indica la nacionalidad del oferente.
    @ManyToOne
    @JoinColumn(name = "nacionalidad", nullable = false)
    private Nacionalidad nacionalidad;

    // Numero de telefono de contacto del oferente
    @Column(length = 20)
    private String telefono;

    // Lugar o ciudad donde reside actualmente el oferente
    @Column(name = "lugar_residencia", length = 150)
    private String lugarResidencia;

    // Bandera que indica si el oferente ha sido autorizado por un administrador
    @Column(nullable = false)
    private Boolean autorizado = false;

    // Ruta o path donde se almacena el curriculum (CV) en formato PDF del oferente
    @Column(length = 255)
    private String curriculum;
}
