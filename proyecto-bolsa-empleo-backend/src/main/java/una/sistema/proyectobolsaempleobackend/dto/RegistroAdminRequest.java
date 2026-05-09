package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroAdminRequest {
    private String correo;
    private String clave;
    private String identificacion;
    private String nombre;
}
