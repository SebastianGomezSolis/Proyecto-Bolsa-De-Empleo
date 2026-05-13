package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Caracteristica.
// Proporciona consultas para navegar el arbol jerarquico de caracteristicas.
public interface CaracteristicaRepository extends CrudRepository<Caracteristica, Integer> {
    List<Caracteristica> findByPadreIsNull();
    List<Caracteristica> findByPadre_Id(Integer padreId);
    boolean existsByNombreIgnoreCaseAndPadre_Id(String nombre, Integer padreId);
    boolean existsByNombreIgnoreCaseAndPadreIsNull(String nombre);
    boolean existsByPadre_Id(Integer padreId);
}
