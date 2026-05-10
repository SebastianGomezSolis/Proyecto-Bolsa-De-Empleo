package una.sistema.proyectobolsaempleobackend.logic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa una nacionalidad.
// Se utiliza para clasificar a los oferentes por su pais de origen.
// Los datos se cargan inicialmente desde un archivo Excel (nacionalidades.xlsx).
@Entity
@Table(name = "nacionalidad")
@Getter
@Setter
public class Nacionalidad {
    // Codigo ISO de 2 letras como clave primaria (ej: "CR", "US", "MX")
    @Id
    @Column(length = 5)
    private String iso;

    // Nombre completo del pais (ej: "Costa Rica", "Estados Unidos")
    @Column(nullable = false, length = 100)
    private String nombre;

    // Descripcion o informacion adicional sobre la nacionalidad/pais
    @Column(length = 255)
    private String descripcion;

    // Codigo ISO de 3 letras del pais (ej: "CRI", "USA")
    @Column(length = 5)
    private String iso3;

    // Codigo numerico asignado al pais (ej: 506 para Costa Rica)
    @Column(name = "codigo_numero")
    private Integer codigoNumero;

    // Codigo telefonico internacional del pais (ej: 506)
    @Column(name = "codigo_telefono")
    private Integer codigoTelefono;
}
