package una.sistema.proyectobolsaempleobackend.dto;

// DTO para la solicitud de inicio de sesion.
// Contiene las credenciales del usuario (correo y contrasena)
// que se envian desde el frontend para autenticarse.
public class LoginRequest {
    // Correo electronico del usuario registrado en el sistema
    private String correo;

    // Contrasena del usuario (se verifica contra el hash almacenado en la BD)
    private String clave;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
