package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Habilidad.
// Proporciona consultas para obtener habilidades de oferentes.
public interface HabilidadRepository extends CrudRepository<Habilidad, Integer> {
    List<Habilidad> findByOferenteId(Integer oferenteId);
}
