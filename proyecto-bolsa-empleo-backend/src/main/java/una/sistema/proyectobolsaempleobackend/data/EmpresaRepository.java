package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;
import java.util.Optional;

// Repositorio para operaciones CRUD con la entidad Empresa.
// Proporciona consultas especificas para empresas.
public interface EmpresaRepository extends CrudRepository<Empresa, Integer> {
    // Busca una empresa por su usuario asociado.
    // Se usa para obtener la empresa a partir de un usuario logueado.
    Optional<Empresa> findByUsuario(Usuario usuario);

    // Busca todas las empresas que no han sido autorizadas aun.
    // Retorna empresas pendientes de aprobacion por el administrador.
    List<Empresa> findByAutorizadoFalse();

    // Verifica si existe una empresa con el correo del usuario especificado.
    boolean existsByUsuarioCorreo(String correo);
}
