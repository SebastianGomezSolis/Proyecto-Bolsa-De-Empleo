package una.sistema.proyectobolsaempleobackend.dto;

import lombok.Getter;
import lombok.Setter;

// DTO para el registro de una nueva empresa.
// Contiene todos los datos necesarios para crear una empresa y su usuario
// asociado en el sistema. Las empresas requieren aprobacion de un administrador.
@Getter
@Setter
public class RegistroEmpresaRequest {
    // Correo electronico unico para la cuenta de la empresa
    private String correo;

    // Contrasena para la cuenta (se almacenara hasheada)
    private String clave;

    // Nombre comercial o razon social de la empresa
    private String nombre;

    // Ubicacion o direccion de la empresa
    private String localizacion;

    // Numero de telefono de contacto
    private String telefono;

    // Descripcion o informacion adicional sobre la empresa
    private String descripcion;
}
