package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

public interface CaracteristicaRepository extends CrudRepository<Caracteristica, Integer> {
    List<Caracteristica> findByPadreIsNull();
    List<Caracteristica> findByPadreId(Integer padreId);
    boolean existsByNombreAndPadreId(String nombre, Integer padreId);
    boolean existsByNombreAndPadreIsNull(String nombre);
    boolean existsByPadreId(Integer padreId);
}
