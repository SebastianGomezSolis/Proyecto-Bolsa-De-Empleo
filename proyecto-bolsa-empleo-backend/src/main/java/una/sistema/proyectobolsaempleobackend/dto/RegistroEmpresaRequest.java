package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroEmpresaRequest {
    private String correo;
    private String clave;
    private String nombre;
    private String localizacion;
    private String telefono;
    private String descripcion;
}
