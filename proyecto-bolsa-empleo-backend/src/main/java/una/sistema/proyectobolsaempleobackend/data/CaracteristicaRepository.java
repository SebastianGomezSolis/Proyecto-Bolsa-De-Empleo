package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad Caracteristica.
// Proporciona consultas para navegar el arbol jerarquico de caracteristicas.
public interface CaracteristicaRepository extends CrudRepository<Caracteristica, Integer> {
    // Busca las caracteristicas raiz (que no tienen padre).
    // Son las categorias de nivel superior del arbol.
    List<Caracteristica> findByPadreIsNull();

    // Busca las subcaracteristicas (hijos) de un padre especifico
    List<Caracteristica> findByPadreId(Integer padreId);

    // Verifica si existe una caracteristica con el nombre especificado bajo un padre
    boolean existsByNombreAndPadreId(String nombre, Integer padreId);

    // Verifica si existe una caracteristica con el nombre especificado como raiz
    boolean existsByNombreAndPadreIsNull(String nombre);

    // Verifica si una caracteristica tiene hijos (no es hoja)
    boolean existsByPadreId(Integer padreId);
}
