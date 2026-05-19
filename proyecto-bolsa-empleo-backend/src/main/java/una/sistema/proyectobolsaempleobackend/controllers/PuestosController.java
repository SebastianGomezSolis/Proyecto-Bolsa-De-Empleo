package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import una.sistema.proyectobolsaempleobackend.dto.BuscarPuestosResponse;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.List;

// Controller para busqueda publica de puestos.
// Proporciona un endpoint para buscar puestos publicos por caracteristicas
// sin necesidad de autenticacion.
@RestController
@RequestMapping("/api")
public class PuestosController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // Busca puestos publicos activos que contengan alguna de las caracteristicas especificadas.
    // Retorna los puestos con sus caracteristicas, las raices del arbol y el tipo de cambio.
    @GetMapping("/puestos/buscar")
    public BuscarPuestosResponse buscarPuestos(@RequestParam(required = false) List<Integer> caracteristicaIds) {
        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            puestos = List.of();
        } else {
            puestos = modeloDatos.getPuestoService().findPublicosActivos().stream()
                    .filter(p -> modeloDatos.getPuestoCaracteristicaService().findByPuesto(p.getId()).stream()
                            .anyMatch(pc -> caracteristicaIds.contains(pc.getCaracteristica().getId())))
                    .toList();
        }

        BuscarPuestosResponse resp = new BuscarPuestosResponse();
        resp.setRaices(modeloDatos.getCaracteristicaService().findRaices());
        resp.setCaracteristicaIds(caracteristicaIds);
        resp.setTipoCambio(obtenerTipoCambio());
        resp.setPuestos(enriquecerPuestos(puestos));
        return resp;
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
