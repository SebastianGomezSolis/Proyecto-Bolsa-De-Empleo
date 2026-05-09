package una.sistema.proyectobolsaempleobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private Integer id;
    private String correo;
    private String rol;
    private Integer referenciaId;
    private String token;
}
