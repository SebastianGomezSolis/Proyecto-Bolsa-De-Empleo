package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;
import java.util.Optional;

// Repositorio para operaciones CRUD con la entidad Oferente.
// Proporciona consultas especificas para oferentes.
public interface OferenteRepository extends CrudRepository<Oferente, Integer> {
    Optional<Oferente> findByUsuario(Usuario usuario);
    Optional<Oferente> findByUsuario_Correo(String correo);
    Optional<Oferente> findByIdentificacion(String identificacion);
    List<Oferente> findByAutorizadoFalse();
    List<Oferente> findByAutorizadoTrue();
    boolean existsByIdentificacion(String identificacion);
    boolean existsByUsuario_Correo(String correo);
    boolean existsByNombreAndPrimerApellido(String nombre, String primerApellido);
}
