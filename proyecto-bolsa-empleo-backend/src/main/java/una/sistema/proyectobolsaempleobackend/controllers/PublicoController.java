package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.List;

@RestController
@RequestMapping("/api/publico")
public class PublicoController {
    @Autowired
    private ModeloDatos modeloDatos;

    @GetMapping("/puestos")
    public PuestosPublicosResponse puestosPublicos() {
        return new PuestosPublicosResponse(
                enriquecerPuestos(modeloDatos.getPuestoService().findPublicosActivos()),
                obtenerTipoCambio()
        );
    }

    @GetMapping("/puestos/ultimos")
    public PuestosPublicosResponse ultimosPuestosPublicos() {
        return new PuestosPublicosResponse(
                enriquecerPuestos(modeloDatos.getPuestoService().findUltimos5Publicos()),
                obtenerTipoCambio()
        );
    }

    @GetMapping("/puestos/buscar")
    public BuscarPuestosResponse buscarPuestosPublicos(
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

        return new BuscarPuestosResponse(
                enriquecerPuestos(puestos),
                modeloDatos.getCaracteristicaService().findRaices(),
                obtenerTipoCambio(),
                caracteristicaIds
        );
    }

    @GetMapping("/puestos/{id}")
    public DetallePuestoResponse detallePuesto(@PathVariable Integer id) {
        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo() || !"publico".equals(puesto.getTipoPublicacion()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado");

        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));
        return new DetallePuestoResponse(puesto, obtenerTipoCambio());
    }

    @GetMapping("/nacionalidades")
    public List nacionalidades() {
        return modeloDatos.getNacionalidadService().findAll();
    }

    @GetMapping("/caracteristicas")
    public List caracteristicas(@RequestParam(required = false) Integer padreId) {
        if (padreId == null) {
            return modeloDatos.getCaracteristicaService().findRaices();
        }
        return modeloDatos.getCaracteristicaService().findHijos(padreId);
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
