package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.NacionalidadRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

import java.util.List;

// Servicio para la gestion de nacionalidades.
// Proporciona metodos para consultar y manipular registros de nacionalidades
// que se cargan desde un archivo Excel al iniciar el sistema.
@Service
public class NacionalidadService {
    // Repositorio para operaciones CRUD con la entidad Nacionalidad
    @Autowired
    private NacionalidadRepository nacionalidadRepository;

    // Retorna todas las nacionalidades registradas en el sistema, ordenadas por nombre.
    // Los datos se cargan inicialmente desde el archivo nacionalidades.xlsx.
    public List<Nacionalidad> findAll() {
        return nacionalidadRepository.findAllByOrderByNombreAsc();
    }

    // Busca una nacionalidad por su codigo ISO.
    // Retorna null si no se encuentra.
    public Nacionalidad findByIso(String iso) {
        return nacionalidadRepository.findById(iso).orElse(null);
    }

    // Retorna la cantidad de nacionalidades registradas.
    // Se usa para verificar si ya se cargaron los datos iniciales.
    public long count() {
        return nacionalidadRepository.count();
    }

    // Guarda o actualiza una nacionalidad en la base de datos
    public Nacionalidad save(Nacionalidad nacionalidad) {
        return nacionalidadRepository.save(nacionalidad);
    }
}
