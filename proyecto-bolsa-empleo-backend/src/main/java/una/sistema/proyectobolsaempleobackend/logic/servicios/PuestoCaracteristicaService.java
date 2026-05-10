package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.PuestoCaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.PuestoCaracteristica;

import java.util.List;

// Servicio para la gestion de relaciones entre puestos y caracteristicas.
// Cada registro representa un requisito (caracteristica con nivel) de un puesto.
@Service
public class PuestoCaracteristicaService {
    // Repositorio para operaciones CRUD con la entidad PuestoCaracteristica
    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    // Guarda o actualiza una relacion puesto-caracteristica en la base de datos
    public PuestoCaracteristica save(PuestoCaracteristica pc) {
        return puestoCaracteristicaRepository.save(pc);
    }

    // Retorna todas las caracteristicas (con niveles) asociadas a un puesto especifico.
    // Se usa para cargar los requisitos de un puesto.
    public List<PuestoCaracteristica> findByPuesto(Integer puestoId) {
        return puestoCaracteristicaRepository.findByPuestoId(puestoId);
    }

    // Elimina una relacion puesto-caracteristica por su ID
    public void deleteById(Integer id) {
        puestoCaracteristicaRepository.deleteById(id);
    }
}
