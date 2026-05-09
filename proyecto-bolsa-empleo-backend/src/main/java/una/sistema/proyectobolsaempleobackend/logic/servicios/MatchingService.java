package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.HabilidadRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.data.PuestoCaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.data.PuestoRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

import java.util.*;

@Service
public class MatchingService {
    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    public Map<Integer, Integer> construirVectorPuesto(Integer puestoId) {
        Map<Integer, Integer> vector = new HashMap<>();
        List<PuestoCaracteristica> caracteristicas = puestoCaracteristicaRepository.findByPuestoId(puestoId);
        for (PuestoCaracteristica pc : caracteristicas) {
            if (pc.getCaracteristica() != null && pc.getCaracteristica().getId() != null) {
                vector.put(pc.getCaracteristica().getId(), pc.getNivelRequerido());
            }
        }
        return vector;
    }

    public Map<Integer, Integer> construirVectorOferente(List<Habilidad> habilidades) {
        Map<Integer, Integer> vector = new HashMap<>();
        if (habilidades == null) return vector;
        for (Habilidad h : habilidades) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId() != null) {
                vector.put(h.getCaracteristica().getId(), h.getNivel());
            }
        }
        return vector;
    }

    public double calcularSimilitudCoseno(Map<Integer, Integer> vectorPuesto, Map<Integer, Integer> vectorOferente) {
        Set<Integer> dimensiones = new HashSet<>();
        dimensiones.addAll(vectorPuesto.keySet());
        dimensiones.addAll(vectorOferente.keySet());

        double productoPunto = 0.0;
        double normaPuesto = 0.0;
        double normaOferente = 0.0;

        for (Integer id : dimensiones) {
            int valorPuesto = vectorPuesto.getOrDefault(id, 0);
            int valorOferente = vectorOferente.getOrDefault(id, 0);

            productoPunto += valorPuesto * valorOferente;
            normaPuesto += valorPuesto * valorPuesto;
            normaOferente += valorOferente * valorOferente;
        }

        if (normaPuesto == 0 || normaOferente == 0) return 0.0;

        return productoPunto / (Math.sqrt(normaPuesto) * Math.sqrt(normaOferente));
    }

    public double calcularSimilitud(Integer puestoId, List<Habilidad> habilidades) {
        Map<Integer, Integer> vectorPuesto = construirVectorPuesto(puestoId);
        Map<Integer, Integer> vectorOferente = construirVectorOferente(habilidades);
        return calcularSimilitudCoseno(vectorPuesto, vectorOferente);
    }

    public double calcularPorcentaje(Integer puestoId, List<Habilidad> habilidades) {
        return calcularSimilitud(puestoId, habilidades) * 100.0;
    }

    public List<CandidatoResultado> buscarCandidatosPorPuesto(Integer puestoId) {
        List<CandidatoResultado> resultados = new ArrayList<>();
        Puesto puesto = puestoRepository.findById(puestoId).orElse(null);

        if (puesto == null) return resultados;

        List<PuestoCaracteristica> requisitos = puestoCaracteristicaRepository.findByPuestoId(puestoId);
        List<Oferente> oferentes = oferenteRepository.findByAutorizadoTrue();

        for (Oferente oferente : oferentes) {
            List<Habilidad> habilidades = habilidadRepository.findByOferenteId(oferente.getId());

            double similitud = calcularSimilitud(puestoId, habilidades);

            int requisitosCumplidos = 0;
            int totalRequisitos = requisitos.size();

            for (PuestoCaracteristica pc : requisitos) {
                for (Habilidad h : habilidades) {
                    if (pc.getCaracteristica().getId().equals(h.getCaracteristica().getId())
                            && h.getNivel() >= pc.getNivelRequerido()) {
                        requisitosCumplidos++;
                        break;
                    }
                }
            }

            CandidatoResultado resultado = new CandidatoResultado();
            resultado.setOferente(oferente);
            resultado.setSimilitud(similitud);
            resultado.setPorcentaje(similitud * 100.0);
            resultado.setRequisitosCumplidos(requisitosCumplidos);
            resultado.setTotalRequisitos(totalRequisitos);

            resultados.add(resultado);
        }

        resultados.sort((a, b) -> Double.compare(b.getSimilitud(), a.getSimilitud()));

        return resultados;
    }
}
