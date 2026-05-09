package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;

import java.util.List;

public interface HabilidadRepository extends CrudRepository<Habilidad, Integer> {
    List<Habilidad> findByOferenteId(Integer oferenteId);
}
