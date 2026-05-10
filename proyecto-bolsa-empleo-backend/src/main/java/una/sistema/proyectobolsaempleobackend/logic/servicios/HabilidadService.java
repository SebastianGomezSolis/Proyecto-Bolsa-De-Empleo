package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.HabilidadRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;

import java.util.List;

// Servicio para la gestion de habilidades de oferentes.
// Las habilidades vinculan a un oferente con una caracteristica (hoja del arbol) y un nivel de competencia.
@Service
public class HabilidadService {
    // Repositorio para operaciones CRUD con la entidad Habilidad
    @Autowired
    private HabilidadRepository habilidadRepository;

    // Retorna todas las habilidades asociadas a un oferente especifico.
    // Se usa para mostrar el perfil de habilidades del candidato.
    public List<Habilidad> findByOferente(Integer oferenteId) {
        return habilidadRepository.findByOferenteId(oferenteId);
    }

    // Busca una habilidad por su ID. Retorna null si no existe.
    public Habilidad findById(Integer id) {
        return habilidadRepository.findById(id).orElse(null);
    }

    // Guarda o actualiza una habilidad en la base de datos
    public void save(Habilidad habilidad) {
        habilidadRepository.save(habilidad);
    }

    // Elimina una habilidad por su ID
    public void deleteById(Integer id) {
        habilidadRepository.deleteById(id);
    }
}
