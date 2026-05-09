package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends CrudRepository<Empresa, Integer> {
    Optional<Empresa> findByUsuario(Usuario usuario);
    List<Empresa> findByAutorizadoFalse();
    boolean existsByUsuarioCorreo(String correo);
}
