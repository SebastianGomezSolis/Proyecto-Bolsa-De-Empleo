package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

public interface NacionalidadRepository extends CrudRepository<Nacionalidad, String> {
}
