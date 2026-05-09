package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.PuestoCaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.PuestoCaracteristica;

import java.util.List;

@Service
public class PuestoCaracteristicaService {
    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    public PuestoCaracteristica save(PuestoCaracteristica pc) {
        return puestoCaracteristicaRepository.save(pc);
    }

    public List<PuestoCaracteristica> findByPuesto(Integer puestoId) {
        return puestoCaracteristicaRepository.findByPuestoId(puestoId);
    }

    public void deleteById(Integer id) {
        puestoCaracteristicaRepository.deleteById(id);
    }
}
