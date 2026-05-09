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

@RestController
@RequestMapping("/api")
public class PuestosController {
    @Autowired
    private ModeloDatos modeloDatos;

    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestos(@RequestParam(required = false) List<Integer> caracteristicaIds) {
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "raices", modeloDatos.getCaracteristicaService().findRaices(),
                    "caracteristicaIds", caracteristicaIds,
                    "tipoCambio", obtenerTipoCambio(),
                    "puestos", List.of()
            ));
        } else {
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


    private List<Puesto> enriquecerPuestos(List<Puesto> puestos) {
        puestos.forEach(p -> p.setCaracteristicas(
                modeloDatos.getPuestoCaracteristicaService().findByPuesto(p.getId())));
        return puestos;
    }

    private Object obtenerTipoCambio() {
        try {
            return modeloDatos.getTipoCambioServicio().obtenerTipoCambio();
        } catch (Exception e) {
            return null;
        }
    }
}
