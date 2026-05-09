package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface OferenteRepository extends CrudRepository<Oferente, Integer> {
    Optional<Oferente> findByUsuario(Usuario usuario);
    List<Oferente> findByAutorizadoFalse();
    List<Oferente> findByAutorizadoTrue();
    boolean existsByIdentificacion(String identificacion);
}
