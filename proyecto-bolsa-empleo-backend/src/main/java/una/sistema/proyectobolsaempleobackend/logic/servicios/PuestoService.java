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

@Service
public class PuestoService {
    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private CaracteristicaService caracteristicaService;

    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    // ─── Consultas ─────────────────────────────────────────────────────────
    public Puesto findById(Integer id) {
        return puestoRepository.findById(id).orElse(null);
    }

    public List<Puesto> findByEmpresa(Integer empresaId) {
        return puestoRepository.findByEmpresaId(empresaId);
    }

    public List<Puesto> findPublicosActivos() {
        return puestoRepository.findByActivoTrueAndTipoPublicacion("publico");
    }

    public List<Puesto> findTodosActivos() {
        return puestoRepository.findByActivoTrue();
    }

    public List<Puesto> findActivosAmbostiposPorCaracteristicas(List<Integer> ids) {
        List<Puesto> publicos = puestoRepository.findByTipoPublicacionAndActivo("publico", true);
        List<Puesto> privados = puestoRepository.findByTipoPublicacionAndActivo("privado", true);

        List<Puesto> todos = new java.util.ArrayList<>();
        todos.addAll(publicos);
        todos.addAll(privados);

        return todos.stream()
                .filter(p -> puestoCaracteristicaRepository.findByPuestoId(p.getId()).stream()
                        .anyMatch(pc -> ids.contains(pc.getCaracteristica().getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Puesto> findUltimos5Publicos() {
        return puestoRepository.findTop5ByTipoPublicacionAndActivoOrderByFechaRegistroDesc("publico", true);
    }

    public List<Puesto> findPorCaracteristicas(List<Integer> ids) {
        Iterable<Puesto> todos = puestoRepository.findAll();
        List<Puesto> resultado = new java.util.ArrayList<>();

        for (Puesto p : todos) {
            if (!p.getActivo()) continue;
            boolean coincide = puestoCaracteristicaRepository.findByPuestoId(p.getId()).stream()
                    .anyMatch(pc -> ids.contains(pc.getCaracteristica().getId()));
            if (coincide) resultado.add(p);
        }
        return resultado;
    }

    public List<Puesto> findByMesYAnio(int mes, int anio) {
        return puestoRepository.findByMesYAnio(mes, anio);
    }

    public List<Puesto> findPorFechaRegistroEntre(LocalDateTime inicio, LocalDateTime fin) {
        return puestoRepository.findByFechaRegistroBetween(inicio, fin);
    }

    // ─── Modificaciones ────────────────────────────────────────────────────
    public void save(Puesto puesto) {
        puestoRepository.save(puesto);
    }

    @Transactional
    public Puesto activar(Integer id) {
        Puesto puesto = findById(id);
        if (puesto != null) {
            puesto.setActivo(true);
            puestoRepository.save(puesto);
        }
        return puesto;
    }

    public String desactivar(Integer id) {
        Puesto puesto = findById(id);
        if (puesto == null) return "Puesto no encontrado";
        puesto.setActivo(false);
        puestoRepository.save(puesto);
        return null;
    }

    // ─── Creación completa con características ──────────────────────────────
    @Transactional
    public Puesto crearConCaracteristicas(Puesto puesto, List<Integer> caracteristicaIds, Map<String, String> niveles) {
        if (puesto.getEmpresa() == null)
            throw new IllegalArgumentException("La empresa autenticada es obligatoria.");

        if (puesto.getDescripcion() == null || puesto.getDescripcion().isBlank())
            throw new IllegalArgumentException("La descripción del puesto es obligatoria.");

        if (puesto.getSalario() == null || puesto.getSalario().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El salario debe ser mayor que cero.");

        String tipo = (puesto.getTipoPublicacion() == null || puesto.getTipoPublicacion().isBlank())
                ? "publico" : puesto.getTipoPublicacion().trim().toLowerCase();

        if (!tipo.equals("publico") && !tipo.equals("privado"))
            throw new IllegalArgumentException("El tipo de publicación no es válido.");

        if (caracteristicaIds == null || caracteristicaIds.isEmpty())
            throw new IllegalArgumentException("Debe seleccionar al menos una característica requerida.");

        puesto.setTipoPublicacion(tipo);
        puesto.setActivo(true);
        puesto.setFechaRegistro(LocalDateTime.now());
        puesto = puestoRepository.save(puesto);

        Set<Integer> procesadas = new HashSet<>();

        for (Integer caracteristicaId : caracteristicaIds) {
            if (caracteristicaId == null || !procesadas.add(caracteristicaId)) continue;

            Caracteristica caracteristica = caracteristicaService.findById(caracteristicaId);
            if (caracteristica == null)
                throw new IllegalArgumentException("Se seleccionó una característica que no existe.");

            if (!caracteristica.isHoja())
                throw new IllegalArgumentException(
                        "Solo se pueden seleccionar características finales (hojas) del árbol.");

            String nivelTexto = niveles != null ? niveles.get("nivel_" + caracteristicaId) : null;
            if (nivelTexto == null || nivelTexto.isBlank())
                throw new IllegalArgumentException(
                        "Debe indicar el nivel requerido para cada característica seleccionada.");

            int nivel;
            try {
                nivel = Integer.parseInt(nivelTexto);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El nivel requerido debe ser un número válido.");
            }

            if (nivel < 1 || nivel > 5)
                throw new IllegalArgumentException("El nivel requerido debe estar entre 1 y 5.");

            PuestoCaracteristica pc = new PuestoCaracteristica();
            pc.setPuesto(puesto);
            pc.setCaracteristica(caracteristica);
            pc.setNivelRequerido(nivel);
            puestoCaracteristicaRepository.save(pc);
        }

        return puesto;
    }
}
