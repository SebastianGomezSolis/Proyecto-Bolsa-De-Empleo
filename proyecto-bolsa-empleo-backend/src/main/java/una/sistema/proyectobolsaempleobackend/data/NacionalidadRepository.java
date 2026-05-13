package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Nacionalidad.
// La clave primaria es String (codigo ISO) en lugar de Integer.
public interface NacionalidadRepository extends CrudRepository<Nacionalidad, String> {
    // Retorna todas las nacionalidades ordenadas por nombre ascendente.
    // Se usa para mostrar listas ordenadas en formularios de registro.
    List<Nacionalidad> findAllByOrderByNombreAsc();
}
