package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroOferenteRequest {
    private String correo;
    private String clave;
    private String identificacion;
    private String nombre;
    private String primerApellido;
    private String isoNacionalidad;
    private String telefono;
    private String lugarResidencia;
}
