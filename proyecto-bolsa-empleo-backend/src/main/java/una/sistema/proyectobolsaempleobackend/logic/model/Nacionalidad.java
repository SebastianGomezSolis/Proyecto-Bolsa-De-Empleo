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
    @Id
    @Column(length = 5)
    private String iso;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 5)
    private String iso3;

    @Column(name = "codigo_numero")
    private Integer codigoNumero;

    @Column(name = "codigo_telefono")
    private Integer codigoTelefono;
}
