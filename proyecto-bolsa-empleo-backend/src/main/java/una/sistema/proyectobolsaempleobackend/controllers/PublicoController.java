package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publico")
public class PublicoController {
    @Autowired
    private ModeloDatos modeloDatos;

    @GetMapping("/puestos")
    public ResponseEntity<?> puestosPublicos() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(modeloDatos.getPuestoService().findPublicosActivos()));
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/puestos/ultimos")
    public ResponseEntity<?> ultimosPuestosPublicos() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(modeloDatos.getPuestoService().findUltimos5Publicos()));
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestosPublicos(
            @RequestParam(required = false) List<Integer> caracteristicaIds) {

        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            puestos = List.of();
        } else {
            puestos = modeloDatos.getPuestoService().findPublicosActivos().stream()
                    .filter(p -> modeloDatos.getPuestoCaracteristicaService()
                            .findByPuesto(p.getId()).stream()
                            .anyMatch(pc -> caracteristicaIds.contains(pc.getCaracteristica().getId())))
                    .toList();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(puestos));
        resp.put("raices", modeloDatos.getCaracteristicaService().findRaices());
        resp.put("tipoCambio", obtenerTipoCambio());
        resp.put("caracteristicaIds", caracteristicaIds);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/puestos/{id}")
    public ResponseEntity<?> detallePuesto(@PathVariable Integer id) {
        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo() || !"publico".equals(puesto.getTipoPublicacion()))
            return ResponseEntity.notFound().build();

        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        Map<String, Object> resp = new HashMap<>();
        resp.put("puesto", puesto);
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/nacionalidades")
    public ResponseEntity<?> nacionalidades() {
        return ResponseEntity.ok(modeloDatos.getNacionalidadService().findAll());
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(@RequestParam(required = false) Integer padreId) {
        if (padreId == null) {
            return ResponseEntity.ok(modeloDatos.getCaracteristicaService().findRaices());
        }
        return ResponseEntity.ok(modeloDatos.getCaracteristicaService().findHijos(padreId));
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
