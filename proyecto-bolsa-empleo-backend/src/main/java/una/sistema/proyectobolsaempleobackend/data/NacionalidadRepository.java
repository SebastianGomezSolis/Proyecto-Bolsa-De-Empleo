package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.repository.CrudRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

// Repositorio para operaciones CRUD con la entidad Nacionalidad.
// La clave primaria es String (codigo ISO) en lugar de Integer.
public interface NacionalidadRepository extends CrudRepository<Nacionalidad, String> {
    // No define metodos adicionales ya que CrudRepository proporciona
    // los necesarios: findById, save, deleteById, count, etc.
}
