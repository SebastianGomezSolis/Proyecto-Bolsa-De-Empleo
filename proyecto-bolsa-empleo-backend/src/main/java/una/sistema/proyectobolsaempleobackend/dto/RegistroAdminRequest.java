package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

// DTO para el registro de un nuevo administrador.
// Contiene los datos necesarios para crear un administrador y su usuario.
// Este DTO se usa para registro de admins adicionales (no el inicial).
@Getter
@Setter
public class RegistroAdminRequest {
    // Correo electronico unico para la cuenta del administrador
    private String correo;

    // Contrasena para la cuenta (se almacenara hasheada)
    private String clave;

    // Numero de identificacion unico del administrador
    private String identificacion;

    // Nombre completo del administrador
    private String nombre;
}
