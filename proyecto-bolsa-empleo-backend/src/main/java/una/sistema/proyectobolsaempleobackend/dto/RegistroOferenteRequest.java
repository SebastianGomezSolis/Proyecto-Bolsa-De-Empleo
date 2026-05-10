package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

// DTO para el registro de un nuevo oferente (candidato).
// Contiene los datos personales del oferente, sus credenciales de acceso
// y la referencia a su nacionalidad. Requiere aprobacion de un administrador.
@Getter
@Setter
public class RegistroOferenteRequest {
    // Correo electronico unico para la cuenta del oferente
    private String correo;

    // Contrasena para la cuenta (se almacenara hasheada)
    private String clave;

    // Numero de identificacion unico del oferente (cedula, pasaporte, etc.)
    private String identificacion;

    // Nombre(s) completo(s) del oferente
    private String nombre;

    // Primer apellido del oferente
    private String primerApellido;

    // Codigo ISO de la nacionalidad del oferente (ej: "CR", "US")
    private String isoNacionalidad;

    // Numero de telefono de contacto del oferente
    private String telefono;

    // Ciudad o lugar donde reside actualmente el oferente
    private String lugarResidencia;
}
