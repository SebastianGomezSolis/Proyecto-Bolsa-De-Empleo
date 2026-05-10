package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.Optional;

// Repositorio para operaciones CRUD con la entidad Usuario.
public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {
    // Busca un usuario por su correo electronico.
    // Retorna un Optional que contiene el usuario si existe.
    Optional<Usuario> findByCorreo(String correo);

    // Verifica si existe un usuario con el correo especificado.
    // Retorna true si existe, false en caso contrario.
    boolean existsByCorreo(String correo);
}
