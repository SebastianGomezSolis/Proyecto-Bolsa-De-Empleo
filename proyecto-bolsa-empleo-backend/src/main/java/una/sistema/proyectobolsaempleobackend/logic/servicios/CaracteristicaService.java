package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import una.sistema.proyectobolsaempleobackend.data.CaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.ArrayList;
import java.util.List;

// Servicio para la gestion del arbol de caracteristicas.
// Las caracteristicas forman una estructura jerarquica (padre-hijo) que permite categorizar habilidades y requisitos de puestos.
@Service
public class CaracteristicaService {
    // Repositorio para operaciones CRUD con la entidad Caracteristica
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    // Busca una caracteristica por su ID. Retorna null si no existe.
    public Caracteristica findById(Integer id) {
        return caracteristicaRepository.findById(id).orElse(null);
    }

    // Retorna todas las caracteristicas almacenadas en la base de datos
    public List<Caracteristica> findAll() {
        List<Caracteristica> lista = new ArrayList<>();
        caracteristicaRepository.findAll().forEach(lista::add);
        return lista;
    }

    // Retorna las caracteristicas raiz (que no tienen padre).
    // Son las categorias de nivel superior del arbol.
    public List<Caracteristica> findRaices() {
        return caracteristicaRepository.findByPadreIsNull();
    }

    // Retorna las subcaracteristicas (hijos) de una caracteristica padre.
    public List<Caracteristica> findHijos(Integer padreId) {
        return caracteristicaRepository.findByPadreId(padreId);
    }

    // Indica si una caracteristica es una hoja (no tiene hijos).
    // Las hojas son las unicas que pueden usarse como habilidades o requisitos.
    // La anotacion @Transactional(readOnly = true) optimiza la lectura.
    @Transactional(readOnly = true)
    public boolean isHoja(Integer id) {
        return !caracteristicaRepository.existsByPadreId(id);
    }

    // Verifica si ya existe una caracteristica con el mismo nombre
    // en el mismo nivel (bajo el mismo padre o como raiz).
    public boolean existeEnMismoNivel(String nombre, Integer padreId) {
        if (padreId == null) {
            // Si no hay padre, buscar en las raices
            return caracteristicaRepository.existsByNombreAndPadreIsNull(nombre);
        }
        // Si hay padre, buscar entre sus hijos
        return caracteristicaRepository.existsByNombreAndPadreId(nombre, padreId);
    }

    // Guarda o actualiza una caracteristica en la base de datos
    public void save(Caracteristica caracteristica) {
        caracteristicaRepository.save(caracteristica);
    }
}
