package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

// DTO para la solicitud de inicio de sesion.
// Contiene las credenciales del usuario (correo y contrasena)
// que se envian desde el frontend para autenticarse.
@Getter
@Setter
public class LoginRequest {
    // Correo electronico del usuario registrado en el sistema
    private String correo;

    // Contrasena del usuario (se verifica contra el hash almacenado en la BD)
    private String clave;
}
