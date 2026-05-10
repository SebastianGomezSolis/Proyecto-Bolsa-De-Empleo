package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Entidad que representa un puesto de trabajo publicado por una empresa.
// Los puestos pueden ser publicos o privados y tienen requisitos
// expresados como caracteristicas con niveles requeridos.
@Entity
@Table(name = "puesto")
@Getter
@Setter
public class Puesto {
    // Clave primaria unica del puesto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Descripcion detallada del puesto (requisitos, responsabilidades, etc.)
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Salario ofrecido para el puesto, con precision de 12 digitos y 2 decimales
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salario;

    // Tipo de publicacion del puesto: "publico" (visible para todos) o "privado" (solo para oferentes)
    @Column(name = "tipo_publicacion", nullable = false, length = 20)
    private String tipoPublicacion = "publico";

    // Relacion muchos a uno con la entidad Empresa.
    // Indica que empresa ha publicado este puesto.
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // Bandera que indica si el puesto esta activo (visible) o inactivo (oculto)
    @Column(nullable = false)
    private Boolean activo = true;

    // Marca temporal que registra automaticamente la fecha de creacion del puesto
    // cuando se persiste por primera vez en la base de datos.
    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Campo transitorio (no persiste en BD) que almacena la lista de
    // caracteristicas con niveles requeridos asociados a este puesto.
    // Se usa para enviar datos al frontend sin guardar en la BD.
    @Transient
    private List<PuestoCaracteristica> caracteristicas;
}
