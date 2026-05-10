package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.PuestoCaracteristica;

import java.util.List;

// Repositorio para operaciones CRUD con la entidad PuestoCaracteristica.
// Proporciona consultas para obtener los requisitos de un puesto.
public interface PuestoCaracteristicaRepository extends CrudRepository<PuestoCaracteristica, Integer> {
    List<PuestoCaracteristica> findByPuestoId(Integer puestoId);
}
