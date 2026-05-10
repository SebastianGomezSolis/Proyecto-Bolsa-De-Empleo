package una.sistema.proyectobolsaempleobackend.logic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa a un usuario en el sistema.
// Cada usuario tiene credenciales de acceso (correo y clave)
// y un rol que determina sus permisos y privilegios dentro del sistema.
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {
    // Clave primaria auto-generada mediante estrategia de identidad (autoincremento en la BD)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Correo electronico unico del usuario, usado como identificador de inicio de sesion
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Clave o contrasena del usuario (se almacena hasheada con BCrypt para seguridad)
    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String clave;

    // Enum que representa el tipo de rol del usuario (ADMIN, EMPRESA u OFERENTE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // Bandera que indica si el usuario esta activo o deshabilitado en el sistema
    @Column(nullable = false)
    private Boolean activo = true;
}
