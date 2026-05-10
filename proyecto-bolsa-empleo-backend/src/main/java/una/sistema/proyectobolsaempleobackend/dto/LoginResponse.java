package una.sistema.proyectobolsaempleobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// DTO para la respuesta exitosa de inicio de sesion.
// Contiene la informacion del usuario autenticado y el token JWT generado.
// Se devuelve al frontend tras un login exitoso.
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    // ID unico del usuario en la base de datos
    private Integer id;

    // Correo electronico del usuario autenticado
    private String correo;

    // Nombre del rol del usuario (ADMIN, EMPRESA u OFERENTE)
    private String rol;

    // ID de referencia que vincula al usuario con su entidad especifica
    // (Administrador, Empresa u Oferente)
    private Integer referenciaId;

    // Token JWT generado para autenticar requests subsecuentes
    private String token;
}
