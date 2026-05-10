package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Habilidad.
// Proporciona consultas para obtener habilidades de oferentes.
public interface HabilidadRepository extends CrudRepository<Habilidad, Integer> {
    // Busca todas las habilidades pertencientes a un oferente especifico.
    // Retorna una lista con las habilidades registradas por ese candidato.
    List<Habilidad> findByOferenteId(Integer oferenteId);
}
