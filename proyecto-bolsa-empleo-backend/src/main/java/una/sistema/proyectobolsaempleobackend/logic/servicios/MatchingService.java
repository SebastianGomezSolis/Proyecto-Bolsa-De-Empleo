package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.HabilidadRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.data.PuestoCaracteristicaRepository;
import una.sistema.proyectobolsaempleobackend.data.PuestoRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

import java.util.*;

// Servicio que implementa el algoritmo de matching (emparejamiento) entre puestos de trabajo y candidatos (oferentes).
// Utiliza la similitud del coseno para calcular compatibilidad.
@Service
public class MatchingService {
    // Repositorio para consultar puestos
    @Autowired
    private PuestoRepository puestoRepository;

    // Repositorio para consultar oferentes
    @Autowired
    private OferenteRepository oferenteRepository;

    // Repositorio para consultar habilidades
    @Autowired
    private HabilidadRepository habilidadRepository;

    // Repositorio para consultar relaciones puesto-caracteristica
    @Autowired
    private PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    // Construye un vector numerico que representa los requisitos de un puesto.
    // Cada dimension es el ID de una caracteristica y el valor es el nivel requerido.
    // Esto se usa para el calculo de similitud con coseno.
    public Map<Integer, Integer> construirVectorPuesto(Integer puestoId) {
        Map<Integer, Integer> vector = new HashMap<>();
        // Obtener todas las caracteristicas con niveles del puesto
        List<PuestoCaracteristica> caracteristicas = puestoCaracteristicaRepository.findByPuestoId(puestoId);
        for (PuestoCaracteristica pc : caracteristicas) {
            if (pc.getCaracteristica() != null && pc.getCaracteristica().getId() != null) {
                // Agregar al vector: ID caracteristica -> nivel requerido
                vector.put(pc.getCaracteristica().getId(), pc.getNivelRequerido());
            }
        }
        return vector;
    }

    // Construye un vector numerico que representa las habilidades de un oferente.
    // Cada dimension es el ID de una caracteristica y el valor es el nivel del oferente.
    public Map<Integer, Integer> construirVectorOferente(List<Habilidad> habilidades) {
        Map<Integer, Integer> vector = new HashMap<>();
        if (habilidades == null) return vector;
        for (Habilidad h : habilidades) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId() != null) {
                // Agregar al vector: ID caracteristica -> nivel del oferente
                vector.put(h.getCaracteristica().getId(), h.getNivel());
            }
        }
        return vector;
    }

    // Calcula la similitud del coseno entre dos vectores.
    // Retorna un valor entre 0.0 (sin compatibilidad) y 1.0 (completa compatibilidad).
    // La formula es: cos(theta) = (A . B) / (||A|| * ||B||)
    public double calcularSimilitudCoseno(Map<Integer, Integer> vectorPuesto, Map<Integer, Integer> vectorOferente) {
        // Obtener todas las dimensiones (IDs de caracteristicas) presentes en ambos vectores
        Set<Integer> dimensiones = new HashSet<>();
        dimensiones.addAll(vectorPuesto.keySet());
        dimensiones.addAll(vectorOferente.keySet());

        // Inicializar variables para el calculo
        double productoPunto = 0.0;    // Numerador: suma de productos componente a componente
        double normaPuesto = 0.0;       // Magnitud del vector del puesto
        double normaOferente = 0.0;      // Magnitud del vector del oferente

        // Calcular producto punto y normas (magnitudes al cuadrado)
        for (Integer id : dimensiones) {
            int valorPuesto = vectorPuesto.getOrDefault(id, 0);
            int valorOferente = vectorOferente.getOrDefault(id, 0);

            productoPunto += valorPuesto * valorOferente;
            normaPuesto += valorPuesto * valorPuesto;
            normaOferente += valorOferente * valorOferente;
        }

        // Evitar division por cero si alguno de los vectores es nulo
        if (normaPuesto == 0 || normaOferente == 0) return 0.0;

        // Retornar similitud del coseno: producto punto / (normaPuesto * normaOferente)
        return productoPunto / (Math.sqrt(normaPuesto) * Math.sqrt(normaOferente));
    }

    // Calcula la similitud (coseno) entre un puesto y las habilidades de un oferente.
    // Convierte las listas a vectores y llama al calculo de coseno.
    public double calcularSimilitud(Integer puestoId, List<Habilidad> habilidades) {
        Map<Integer, Integer> vectorPuesto = construirVectorPuesto(puestoId);
        Map<Integer, Integer> vectorOferente = construirVectorOferente(habilidades);
        return calcularSimilitudCoseno(vectorPuesto, vectorOferente);
    }

    // Calcula el porcentaje de compatibilidad (similitud * 100).
    public double calcularPorcentaje(Integer puestoId, List<Habilidad> habilidades) {
        return calcularSimilitud(puestoId, habilidades) * 100.0;
    }

    // Busca y rankea candidatos para un puesto especifico.
    // Itera sobre todos los oferentes autorizados, calcula su compatibilidad
    // y retorna una lista ordenada de mayor a menor similitud.
    public List<CandidatoResultado> buscarCandidatosPorPuesto(Integer puestoId) {
        List<CandidatoResultado> resultados = new ArrayList<>();

        // Obtener el puesto por ID
        Puesto puesto = puestoRepository.findById(puestoId).orElse(null);
        if (puesto == null) return resultados;

        // Obtener los requisitos (caracteristicas con niveles) del puesto
        List<PuestoCaracteristica> requisitos = puestoCaracteristicaRepository.findByPuestoId(puestoId);

        // Obtener todos los oferentes autorizados
        List<Oferente> oferentes = oferenteRepository.findByAutorizadoTrue();

        // Evaluar cada oferente
        for (Oferente oferente : oferentes) {
            // Obtener las habilidades del oferente
            List<Habilidad> habilidades = habilidadRepository.findByOferenteId(oferente.getId());

            // Calcular similitud del coseno
            double similitud = calcularSimilitud(puestoId, habilidades);

            // Contar requisitos cumplidos (nivel oferente >= nivel requerido)
            int requisitosCumplidos = 0;
            int totalRequisitos = requisitos.size();

            for (PuestoCaracteristica pc : requisitos) {
                for (Habilidad h : habilidades) {
                    // Verificar si es la misma caracteristica y si el nivel es suficiente
                    if (pc.getCaracteristica().getId().equals(h.getCaracteristica().getId())
                            && h.getNivel() >= pc.getNivelRequerido()) {
                        requisitosCumplidos++;
                        break; // Ya cumplio este requisito, pasar al siguiente
                    }
                }
            }

            // Crear resultado y agregarlo a la lista
            CandidatoResultado resultado = new CandidatoResultado();
            resultado.setOferente(oferente);
            resultado.setSimilitud(similitud);
            resultado.setPorcentaje(similitud * 100.0);
            resultado.setRequisitosCumplidos(requisitosCumplidos);
            resultado.setTotalRequisitos(totalRequisitos);

            resultados.add(resultado);
        }

        // Ordenar por similitud descendente (mas compatible primero)
        resultados.sort((a, b) -> Double.compare(b.getSimilitud(), a.getSimilitud()));

        return resultados;
    }
}
