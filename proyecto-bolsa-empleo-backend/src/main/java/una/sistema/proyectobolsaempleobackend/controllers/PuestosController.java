package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.List;
import java.util.Map;

// Controller REST para busqueda publica de puestos.
// Proporciona un endpoint para buscar puestos publicos por caracteristicas
// sin necesidad de autenticacion.
@RestController
@RequestMapping("/api")
public class PuestosController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // GET /api/puestos/buscar
    // Busca puestos publicos activos que contengan alguna de las caracteristicas especificadas.
    // Retorna los puestos con sus caracteristicas, las raices del arbol y el tipo de cambio.
    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestos(@RequestParam(required = false) List<Integer> caracteristicaIds) {
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            // Si no hay filtros, retornar estructura basica con raices disponibles
            return ResponseEntity.ok(Map.of(
                    "raices", modeloDatos.getCaracteristicaService().findRaices(),
                    "caracteristicaIds", caracteristicaIds,
                    "tipoCambio", obtenerTipoCambio(),
                    "puestos", List.of()
            ));
        } else {
            // Filtrar puestos publicos que tengan alguna de las caracteristicas seleccionadas
            List<Puesto> puestos = modeloDatos.getPuestoService().findPublicosActivos().stream()
                    .filter(p -> modeloDatos.getPuestoCaracteristicaService().findByPuesto(p.getId()).stream()
                            .anyMatch(pc -> caracteristicaIds.contains(pc.getCaracteristica().getId())))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "raices", modeloDatos.getCaracteristicaService().findRaices(),
                    "caracteristicaIds", caracteristicaIds,
                    "tipoCambio", obtenerTipoCambio(),
                    "puestos", enriquecerPuestos(puestos)
            ));
        }
    }

    // Agrega las caracteristicas a cada puesto para enviar al frontend
    private List<Puesto> enriquecerPuestos(List<Puesto> puestos) {
        puestos.forEach(p -> p.setCaracteristicas(
                modeloDatos.getPuestoCaracteristicaService().findByPuesto(p.getId())));
        return puestos;
    }

    // Obtiene el tipo de cambio del dolar, manejando errores
    private Object obtenerTipoCambio() {
        try {
            return modeloDatos.getTipoCambioServicio().obtenerTipoCambio();
        } catch (Exception e) {
            return null;
        }
    }
}
