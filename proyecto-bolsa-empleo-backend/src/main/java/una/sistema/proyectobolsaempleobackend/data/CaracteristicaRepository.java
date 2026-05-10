package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Caracteristica.
// Proporciona consultas para navegar el arbol jerarquico de caracteristicas.
public interface CaracteristicaRepository extends CrudRepository<Caracteristica, Integer> {
    List<Caracteristica> findByPadreIsNull();
    List<Caracteristica> findByPadreId(Integer padreId);
    boolean existsByNombreAndPadreId(String nombre, Integer padreId);
    boolean existsByNombreAndPadreIsNull(String nombre);
    boolean existsByPadreId(Integer padreId);
}
