package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa a una empresa registrada en el sistema.
// Las empresas pueden crear y administrar ofertas de empleo (puestos).
@Entity
@Table(name = "empresa")
@Getter
@Setter
public class Empresa {
    // Clave primaria unica de la empresa, generada automaticamente por la BD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacion uno a uno con la entidad Usuario.
    // Cada empresa tiene un usuario asociado para autenticacion.
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Nombre comercial o razon social de la empresa
    @Column(nullable = false, length = 100)
    private String nombre;

    // Ubicacion fisica o direccion de la empresa (ciudad, provincia, etc.)
    @Column(length = 150)
    private String localizacion;

    // Numero de telefono de contacto de la empresa
    @Column(length = 20)
    private String telefono;

    // Descripcion o informacion adicional sobre la empresa.
    // Se usa @Lob para almacenar textos largos en la BD.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Bandera que indica si la empresa ha sido autorizada por un administrador.
    // Solo las empresas autorizadas pueden usar el sistema.
    @Column(nullable = false)
    private Boolean autorizado = false;
}
