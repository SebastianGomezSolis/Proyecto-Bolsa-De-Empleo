package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.Optional;

public interface AdministradorRepository extends CrudRepository<Administrador, Integer> {
    Optional<Administrador> findByUsuario(Usuario usuario);
}
