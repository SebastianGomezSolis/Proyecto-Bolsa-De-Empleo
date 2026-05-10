package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.Optional;

// Repositorio para operaciones CRUD con la entidad Administrador.
// Proporciona consultas especificas para administradores.
public interface AdministradorRepository extends CrudRepository<Administrador, Integer> {
    // Busca un administrador por su usuario asociado.
    // Se usa para obtener el ID de referencia al iniciar sesion.
    Optional<Administrador> findByUsuario(Usuario usuario);
}
