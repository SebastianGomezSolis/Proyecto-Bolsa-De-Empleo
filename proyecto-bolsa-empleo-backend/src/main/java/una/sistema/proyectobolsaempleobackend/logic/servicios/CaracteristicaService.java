package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import una.sistema.proyectobolsaempleobackend.data.CaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.ArrayList;
import java.util.List;

@Service
public class CaracteristicaService {
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    public Caracteristica findById(Integer id) {
        return caracteristicaRepository.findById(id).orElse(null);
    }

    public List<Caracteristica> findAll() {
        List<Caracteristica> lista = new ArrayList<>();
        caracteristicaRepository.findAll().forEach(lista::add);
        return lista;
    }

    public List<Caracteristica> findRaices() {
        return caracteristicaRepository.findByPadreIsNull();
    }

    public List<Caracteristica> findHijos(Integer padreId) {
        return caracteristicaRepository.findByPadreId(padreId);
    }

    @Transactional(readOnly = true)
    public boolean isHoja(Integer id) {
        return !caracteristicaRepository.existsByPadreId(id);
    }

    public boolean existeEnMismoNivel(String nombre, Integer padreId) {
        if (padreId == null) {
            return caracteristicaRepository.existsByNombreAndPadreIsNull(nombre);
        }
        return caracteristicaRepository.existsByNombreAndPadreId(nombre, padreId);
    }

    public void save(Caracteristica caracteristica) {
        caracteristicaRepository.save(caracteristica);
    }
}
