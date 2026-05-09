package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;
import una.sistema.proyectobolsaempleobackend.logic.model.SesionUsuarioBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private ModeloDatos modeloDatos;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // ─── Empresas pendientes ────────────────────────────────────────────────
    @GetMapping("/empresas/pendientes")
    public ResponseEntity<?> empresasPendientes() {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        return ResponseEntity.ok(modeloDatos.getEmpresaService().findPendientes());
    }

    @PostMapping("/empresas/{id}/autorizar")
    public ResponseEntity<?> autorizarEmpresa(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        String error = modeloDatos.getEmpresaService().autorizar(id);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Empresa autorizada");
    }

    // ─── Oferentes pendientes ───────────────────────────────────────────────
    @GetMapping("/oferentes/pendientes")
    public ResponseEntity<?> oferentesPendientes() {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        return ResponseEntity.ok(modeloDatos.getOferenteService().findPendientes());
    }

    @PostMapping("/oferentes/{id}/autorizar")
    public ResponseEntity<?> autorizarOferente(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        String error = modeloDatos.getOferenteService().autorizar(id);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Oferente autorizado");
    }

    // ─── Características (árbol navegable) ─────────────────────────────────
    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(
            @RequestParam(required = false) Integer actualId) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        Map<String, Object> resp = new HashMap<>();

        if (actualId == null) {
            resp.put("subcategorias", modeloDatos.getCaracteristicaService().findRaices());
            resp.put("actual", null);
            resp.put("ruta", List.of());
        } else {
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) {
                resp.put("subcategorias", modeloDatos.getCaracteristicaService().findRaices());
                resp.put("actual", null);
                resp.put("ruta", List.of());
            } else {
                resp.put("subcategorias", modeloDatos.getCaracteristicaService().findHijos(actualId));
                resp.put("actual", actual);
                resp.put("ruta", construirRuta(actual));
            }
        }

        resp.put("todas", modeloDatos.getCaracteristicaService().findAll());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/caracteristicas")
    public ResponseEntity<?> crearCaracteristica(@RequestBody Map<String, Object> body) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        String nombre  = (String) body.get("nombre");
        Integer padreId = body.get("padreId") != null ? (Integer) body.get("padreId") : null;

        if (nombre == null || nombre.isBlank())
            return ResponseEntity.badRequest().body("El nombre de la característica es obligatorio");

        String nombreLimpio = nombre.trim();

        if (modeloDatos.getCaracteristicaService().existeEnMismoNivel(nombreLimpio, padreId))
            return ResponseEntity.badRequest()
                    .body("Ya existe una característica con ese nombre bajo el mismo padre");

        Caracteristica c = new Caracteristica();
        c.setNombre(nombreLimpio);

        if (padreId != null) {
            Caracteristica padre = modeloDatos.getCaracteristicaService().findById(padreId);
            if (padre == null) return ResponseEntity.badRequest().body("Padre no encontrado");
            c.setPadre(padre);
        }

        modeloDatos.getCaracteristicaService().save(c);
        return ResponseEntity.ok("Característica creada");
    }

    // ─── Reportes PDF ──────────────────────────────────────────────────────
    @GetMapping("/reportes/pdf")
    public ResponseEntity<?> reportePdf(@RequestParam int mes, @RequestParam int anio) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        try {
            byte[] pdf = modeloDatos.getReporteService().generarPdfPuestosPorMesYAnio(mes, anio);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set("Content-Disposition",
                    "inline; filename=\"reporte-" + mes + "-" + anio + ".pdf\"");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generando PDF: " + e.getMessage());
        }
    }

    // ─── Utilidades ────────────────────────────────────────────────────────
    private List<Caracteristica> construirRuta(Caracteristica actual) {
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica cursor = actual;
        while (cursor != null) {
            ruta.add(0, cursor);
            cursor = cursor.getPadre();
        }
        return ruta;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }
}
