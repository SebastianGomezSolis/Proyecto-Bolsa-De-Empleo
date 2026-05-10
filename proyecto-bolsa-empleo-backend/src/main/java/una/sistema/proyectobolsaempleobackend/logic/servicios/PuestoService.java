package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import una.sistema.proyectobolsaempleobackend.data.PuestoCaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.data.PuestoRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;
import una.sistema.proyectobolsaempleobackend.logic.model.PuestoCaracteristica;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Servicio para la gestion de puestos de trabajo.
// Maneja la creacion, consulta, activacion y desactivacion de puestos.
// Los puestos pueden ser publicos o privados y tienen caracteristicas asociadas.
@Service
public class PuestoService {
    // Repositorio para operaciones CRUD con la entidad Puesto
    @Autowired
    private PuestoRepository puestoRepository;

    // Servicio para gestionar caracteristicas (validaciones, busqueda)
    @Autowired
    private CaracteristicaService caracteristicaService;

    // Repositorio para gestionar relaciones puesto-caracteristica
    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    // Busca un puesto por su ID. Retorna null si no existe.
    public Puesto findById(Integer id) {
        return puestoRepository.findById(id).orElse(null);
    }

    // Retorna todos los puestos pertencientes a una empresa especifica
    public List<Puesto> findByEmpresa(Integer empresaId) {
        return puestoRepository.findByEmpresaId(empresaId);
    }

    // Retorna todos los puestos publicos que estan activos
    public List<Puesto> findPublicosActivos() {
        return puestoRepository.findByActivoTrueAndTipoPublicacion("publico");
    }

    // Retorna todos los puestos que estan activos (independiente del tipo)
    public List<Puesto> findTodosActivos() {
        return puestoRepository.findByActivoTrue();
    }

    // Retorna puestos activos (publicos y privados) que contienen
    // al menos una de las caracteristicas especificadas.
    public List<Puesto> findActivosAmbostiposPorCaracteristicas(List<Integer> ids) {
        // Obtener puestos publicos y privados activos por separado
        List<Puesto> publicos = puestoRepository.findByTipoPublicacionAndActivo("publico", true);
        List<Puesto> privados = puestoRepository.findByTipoPublicacionAndActivo("privado", true);

        // Combinar ambas listas
        List<Puesto> todos = new java.util.ArrayList<>();
        todos.addAll(publicos);
        todos.addAll(privados);

        // Filtrar: incluir solo puestos que tengan al menos una caracteristica de la lista
        return todos.stream()
                .filter(p -> puestoCaracteristicaRepository.findByPuestoId(p.getId()).stream()
                        .anyMatch(pc -> ids.contains(pc.getCaracteristica().getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    // Retorna los ultimos 5 puestos publicos ordenados por fecha de registro
    public List<Puesto> findUltimos5Publicos() {
        return puestoRepository.findTop5ByTipoPublicacionAndActivoOrderByFechaRegistroDesc("publico", true);
    }

    // Retorna puestos activos que contienen alguna de las caracteristicas especificadas
    public List<Puesto> findPorCaracteristicas(List<Integer> ids) {
        Iterable<Puesto> todos = puestoRepository.findAll();
        List<Puesto> resultado = new java.util.ArrayList<>();

        for (Puesto p : todos) {
            // Omitir puestos inactivos
            if (!p.getActivo()) continue;

            // Verificar si el puesto tiene alguna caracteristica de la lista
            boolean coincide = puestoCaracteristicaRepository.findByPuestoId(p.getId()).stream()
                    .anyMatch(pc -> ids.contains(pc.getCaracteristica().getId()));
            if (coincide) resultado.add(p);
        }
        return resultado;
    }

    // Retorna puestos registrados en un mes y ano especifico
    public List<Puesto> findByMesYAnio(int mes, int anio) {
        return puestoRepository.findByMesYAnio(mes, anio);
    }

    // Retorna puestos registrados dentro de un rango de fechas
    public List<Puesto> findPorFechaRegistroEntre(LocalDateTime inicio, LocalDateTime fin) {
        return puestoRepository.findByFechaRegistroBetween(inicio, fin);
    }

    // Guarda un puesto en la base de datos
    public void save(Puesto puesto) {
        puestoRepository.save(puesto);
    }

    // Activa un puesto desactivado para que sea visible nuevamente.
    // La anotacion @Transactional asegura consistencia de datos.
    @Transactional
    public Puesto activar(Integer id) {
        Puesto puesto = findById(id);
        if (puesto != null) {
            puesto.setActivo(true);
            puestoRepository.save(puesto);
        }
        return puesto;
    }

    // Desactiva un puesto para ocultarlo de las busquedas.
    // Retorna null si es exitoso, o un mensaje de error.
    public String desactivar(Integer id) {
        Puesto puesto = findById(id);
        if (puesto == null) return "Puesto no encontrado";
        puesto.setActivo(false);
        puestoRepository.save(puesto);
        return null;
    }

    // Crea un puesto junto con sus caracteristicas y niveles requeridos.
    // Valida todos los datos antes de persistir y usa transaccion
    // para garantizar consistencia entre puesto y sus caracteristicas.
    // Lanza IllegalArgumentException si hay errores de validacion.
    @Transactional
    public Puesto crearConCaracteristicas(Puesto puesto, List<Integer> caracteristicaIds, Map<String, String> niveles) {
        // Validar que la empresa este asignada
        if (puesto.getEmpresa() == null)
            throw new IllegalArgumentException("La empresa autenticada es obligatoria.");

        // Validar que la descripcion no este vacia
        if (puesto.getDescripcion() == null || puesto.getDescripcion().isBlank())
            throw new IllegalArgumentException("La descripción del puesto es obligatoria.");

        // Validar que el salario sea mayor que cero
        if (puesto.getSalario() == null || puesto.getSalario().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El salario debe ser mayor que cero.");

        // Asignar tipo de publicacion (default "publico" si no se especifica)
        String tipo = (puesto.getTipoPublicacion() == null || puesto.getTipoPublicacion().isBlank())
                ? "publico" : puesto.getTipoPublicacion().trim().toLowerCase();

        // Validar que el tipo sea "publico" o "privado"
        if (!tipo.equals("publico") && !tipo.equals("privado"))
            throw new IllegalArgumentException("El tipo de publicación no es válido.");

        // Validar que se haya seleccionado al menos una caracteristica
        if (caracteristicaIds == null || caracteristicaIds.isEmpty())
            throw new IllegalArgumentException("Debe seleccionar al menos una característica requerida.");

        // Configurar puesto antes de guardar
        puesto.setTipoPublicacion(tipo);
        puesto.setActivo(true);
        puesto.setFechaRegistro(LocalDateTime.now());
        puesto = puestoRepository.save(puesto);

        // Set para evitar duplicados en la lista de caracteristicas
        Set<Integer> procesadas = new HashSet<>();

        // Procesar cada caracteristica seleccionada
        for (Integer caracteristicaId : caracteristicaIds) {
            // Ignorar IDs nulos o duplicados
            if (caracteristicaId == null || !procesadas.add(caracteristicaId)) continue;

            // Buscar la caracteristica en la BD
            Caracteristica caracteristica = caracteristicaService.findById(caracteristicaId);
            if (caracteristica == null)
                throw new IllegalArgumentException("Se seleccionó una característica que no existe.");

            // Validar que sea una hoja (caracteristica final)
            if (!caracteristica.isHoja())
                throw new IllegalArgumentException(
                        "Solo se pueden seleccionar características finales (hojas) del árbol.");

            // Obtener el nivel requerido desde el mapa (formato "nivel_ID")
            String nivelTexto = niveles != null ? niveles.get("nivel_" + caracteristicaId) : null;
            if (nivelTexto == null || nivelTexto.isBlank())
                throw new IllegalArgumentException(
                        "Debe indicar el nivel requerido para cada característica seleccionada.");

            // Convertir nivel a entero y validar rango
            int nivel;
            try {
                nivel = Integer.parseInt(nivelTexto);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El nivel requerido debe ser un número válido.");
            }

            if (nivel < 1 || nivel > 5)
                throw new IllegalArgumentException("El nivel requerido debe estar entre 1 y 5.");

            // Crear y guardar la relacion puesto-caracteristica
            PuestoCaracteristica pc = new PuestoCaracteristica();
            pc.setPuesto(puesto);
            pc.setCaracteristica(caracteristica);
            pc.setNivelRequerido(nivel);
            puestoCaracteristicaRepository.save(pc);
        }

        return puesto;
    }
}
