package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.HabilidadRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;

import java.util.List;

@Service
public class HabilidadService {
    @Autowired
    private HabilidadRepository habilidadRepository;

    public List<Habilidad> findByOferente(Integer oferenteId) {
        return habilidadRepository.findByOferenteId(oferenteId);
    }

    public Habilidad findById(Integer id) {
        return habilidadRepository.findById(id).orElse(null);
    }

    public void save(Habilidad habilidad) {
        habilidadRepository.save(habilidad);
    }

    public void deleteById(Integer id) {
        habilidadRepository.deleteById(id);
    }
}
