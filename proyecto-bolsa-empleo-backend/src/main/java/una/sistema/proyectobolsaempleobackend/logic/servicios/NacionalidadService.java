package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.NacionalidadRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

import java.util.ArrayList;
import java.util.List;

@Service
public class NacionalidadService {
    @Autowired
    private NacionalidadRepository nacionalidadRepository;

    public List<Nacionalidad> findAll() {
        List<Nacionalidad> lista = new ArrayList<>();
        nacionalidadRepository.findAll().forEach(lista::add);
        return lista;
    }

    public Nacionalidad findByIso(String iso) {
        return nacionalidadRepository.findById(iso).orElse(null);
    }

    public long count() {
        return nacionalidadRepository.count();
    }

    public Nacionalidad save(Nacionalidad nacionalidad) {
        return nacionalidadRepository.save(nacionalidad);
    }
}
