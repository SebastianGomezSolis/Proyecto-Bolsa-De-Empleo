package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.AdministradorRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

// Servicio para la gestion de administradores.
// Proporciona metodos para crear y buscar administradores en el sistema.
@Service
public class AdministradorService {
    // Repositorio para operaciones CRUD con la entidad Administrador
    @Autowired
    private AdministradorRepository administradorRepository;

    // Servicio para gestionar usuarios
    @Autowired
    private UsuarioService usuarioService;

    // Retorna todos los administradores registrados en el sistema
    public Iterable<Administrador> findAll() {
        return administradorRepository.findAll();
    }

    // Busca un administrador por su ID. Retorna null si no existe.
    public Administrador findById(Integer id) {
        return administradorRepository.findById(id).orElse(null);
    }

    // Crea un nuevo administrador en el sistema.
    // Valida campos requeridos, verifica que el correo sea unico,
    // asigna rol ADMIN y usuario activo por defecto.
    // Retorna null si es exitoso, o un mensaje de error.
    public String crear(Administrador admin) {
        // Validar que el usuario no sea null
        if (admin.getUsuario() == null)
            return "El usuario es requerido";

        // Validar correo
        if (admin.getUsuario().getCorreo() == null || admin.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        // Validar contrasena
        if (admin.getUsuario().getClave() == null || admin.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        // Validar identificacion
        if (admin.getIdentificacion() == null || admin.getIdentificacion().isBlank())
            return "La identificación es requerida";

        // Validar nombre
        if (admin.getNombre() == null || admin.getNombre().isBlank())
            return "El nombre es requerido";

        // Verificar que el correo no este ya registrado
        if (usuarioService.existeCorreo(admin.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        // Asignar rol ADMIN al usuario
        admin.getUsuario().setRol(Rol.ADMIN);

        // Activar usuario por defecto (los admins no requieren aprobacion)
        admin.getUsuario().setActivo(true);

        // Guardar usuario con contrasena hasheada
        Usuario guardado = usuarioService.guardar(admin.getUsuario());
        admin.setUsuario(guardado);

        // Guardar administrador
        administradorRepository.save(admin);
        return null;
    }
}
