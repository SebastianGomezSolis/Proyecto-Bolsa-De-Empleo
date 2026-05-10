package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controller REST para endpoints publicos.
// Proporciona acceso a puestos publicos sin necesidad de autenticacion.
// Incluye busqueda, detalle de puestos, nacionalidades y caracteristicas.
@RestController
@RequestMapping("/api/publico")
public class PublicoController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // GET /api/publico/puestos
    // Retorna todos los puestos publicos activos.
    // Incluye las caracteristicas de cada puesto y el tipo de cambio.
    @GetMapping("/puestos")
    public ResponseEntity<?> puestosPublicos() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(modeloDatos.getPuestoService().findPublicosActivos()));
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    // GET /api/publico/puestos/ultimos
    // Retorna los ultimos 5 puestos publicos ordenados por fecha de publicacion.
    @GetMapping("/puestos/ultimos")
    public ResponseEntity<?> ultimosPuestosPublicos() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(modeloDatos.getPuestoService().findUltimos5Publicos()));
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    // GET /api/publico/puestos/buscar
    // Busca puestos publicos que contengan alguna de las caracteristicas especificadas.
    // Retorna puestos enriquecidos con sus caracteristicas.
    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestosPublicos(
            @RequestParam(required = false) List<Integer> caracteristicaIds) {

        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            // Si no hay filtros, retornar lista vacia
            puestos = List.of();
        } else {
            // Filtrar puestos que contengan alguna de las caracteristicas
            puestos = modeloDatos.getPuestoService().findPublicosActivos().stream()
                    .filter(p -> modeloDatos.getPuestoCaracteristicaService()
                            .findByPuesto(p.getId()).stream()
                            .anyMatch(pc -> caracteristicaIds.contains(pc.getCaracteristica().getId())))
                    .toList();
        }

        // Construir respuesta completa
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(puestos));
        resp.put("raices", modeloDatos.getCaracteristicaService().findRaices());
        resp.put("tipoCambio", obtenerTipoCambio());
        resp.put("caracteristicaIds", caracteristicaIds);
        return ResponseEntity.ok(resp);
    }

    // GET /api/publico/puestos/{id}
    // Retorna el detalle de un puesto publico especifico.
    // Debe estar activo y ser de tipo "publico".
    @GetMapping("/puestos/{id}")
    public ResponseEntity<?> detallePuesto(@PathVariable Integer id) {
        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo() || !"publico".equals(puesto.getTipoPublicacion()))
            return ResponseEntity.notFound().build();

        // Cargar caracteristicas del puesto
        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        Map<String, Object> resp = new HashMap<>();
        resp.put("puesto", puesto);
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    // GET /api/publico/nacionalidades
    // Retorna todas las nacionalidades disponibles.
    // Usado para el formulario de registro de oferentes.
    @GetMapping("/nacionalidades")
    public ResponseEntity<?> nacionalidades() {
        return ResponseEntity.ok(modeloDatos.getNacionalidadService().findAll());
    }

    // GET /api/publico/caracteristicas
    // Retorna las caracteristicas raiz o los hijos de una padre especifico.
    // Se usa para construir el arbol de seleccion en el frontend.
    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(@RequestParam(required = false) Integer padreId) {
        if (padreId == null) {
            return ResponseEntity.ok(modeloDatos.getCaracteristicaService().findRaices());
        }
        return ResponseEntity.ok(modeloDatos.getCaracteristicaService().findHijos(padreId));
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
