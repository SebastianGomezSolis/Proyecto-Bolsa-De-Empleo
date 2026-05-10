package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.UsuarioRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

// Servicio para la gestion de usuarios.
// Proporciona metodos para buscar, verificar existencia y guardar usuarios.
// Las contrasenas se hashean automaticamente antes de persistir.
@Service
public class UsuarioService {
    // Repositorio para operaciones de base de datos sobre la entidad Usuario
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Servicio para hashear contrasenas con BCrypt
    @Autowired
    private PasswordHash passwordHash;

    // Busca un usuario por su correo electronico.
    // Retorna null si no se encuentra.
    public Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    // Verifica si existe un usuario registrado con el correo especificado.
    // Retorna true si existe, false en caso contrario.
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    // Guarda un usuario en la base de datos.
    // Hashea la contrasena antes de persistir para seguridad.
    public Usuario guardar(Usuario usuario) {
        usuario.setClave(passwordHash.hash(usuario.getClave()));
        return usuarioRepository.save(usuario);
    }

    // Guarda un usuario sin hashear la contrasena.
    // Usado cuando la contrasena ya viene hasheada (ej: actualizacion de datos de usuario).
    public Usuario guardarSinHash(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
