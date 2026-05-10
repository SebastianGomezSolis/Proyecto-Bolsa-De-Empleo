package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;
import java.util.Optional;

// Repositorio para operaciones CRUD con la entidad Oferente.
// Proporciona consultas especificas para oferentes.
public interface OferenteRepository extends CrudRepository<Oferente, Integer> {
    // Busca un oferente por su usuario asociado.
    // Se usa para obtener el oferente a partir de un usuario logueado.
    Optional<Oferente> findByUsuario(Usuario usuario);

    // Busca todos los oferentes pendientes de autorizacion.
    List<Oferente> findByAutorizadoFalse();

    // Busca todos los oferentes que ya han sido autorizados.
    // Se usa para listar candidatos disponibles para matching.
    List<Oferente> findByAutorizadoTrue();

    // Verifica si existe un oferente con la identificacion especificada.
    boolean existsByIdentificacion(String identificacion);
}
