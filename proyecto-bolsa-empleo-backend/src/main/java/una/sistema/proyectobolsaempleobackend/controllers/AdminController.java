package una.sistema.proyectobolsaempleobackend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private ModeloDatos modeloDatos;

    @GetMapping("/empresas/pendientes")
    public List empresasPendientes() {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return modeloDatos.getEmpresaService().findPendientes();
    }

    @PostMapping("/empresas/{id}/autorizar")
    public String autorizarEmpresa(@PathVariable Integer id) {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        String error = modeloDatos.getEmpresaService().autorizar(id);
        if (error != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        return "Empresa autorizada";
    }

    @GetMapping("/oferentes/pendientes")
    public List oferentesPendientes() {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return modeloDatos.getOferenteService().findPendientes();
    }

    @PostMapping("/oferentes/{id}/autorizar")
    public String autorizarOferente(@PathVariable Integer id) {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        String error = modeloDatos.getOferenteService().autorizar(id);
        if (error != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        return "Oferente autorizado";
    }

    @GetMapping("/caracteristicas")
    @Transactional(readOnly = true)
    public CaracteristicasAdminResponse caracteristicas(@RequestParam(required = false) Integer actualId) {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        CaracteristicasAdminResponse resp = new CaracteristicasAdminResponse();
        resp.setTodas(modeloDatos.getCaracteristicaService().findAll());

        if (actualId == null) {
            resp.setSubcategorias(modeloDatos.getCaracteristicaService().findRaices());
            resp.setActual(null);
            resp.setRuta(List.of());
        } else {
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) {
                resp.setSubcategorias(modeloDatos.getCaracteristicaService().findRaices());
                resp.setActual(null);
                resp.setRuta(List.of());
            } else {
                resp.setSubcategorias(modeloDatos.getCaracteristicaService().findHijos(actualId));
                resp.setActual(actual);
                resp.setRuta(construirRuta(actual));
            }
        }

        return resp;
    }

    @PostMapping("/caracteristicas")
    public String crearCaracteristica(@RequestBody CrearCaracteristicaRequest req) {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        String nombre  = req.getNombre();
        Integer padreId = req.getPadreId();

        if (nombre == null || nombre.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la característica es obligatorio");

        String nombreLimpio = nombre.trim();

        if (modeloDatos.getCaracteristicaService().existeEnMismoNivel(nombreLimpio, padreId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe una característica con ese nombre bajo el mismo padre");

        Caracteristica c = new Caracteristica();
        c.setNombre(nombreLimpio);

        if (padreId != null) {
            Caracteristica padre = modeloDatos.getCaracteristicaService().findById(padreId);
            if (padre == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Padre no encontrado");
            c.setPadre(padre);
        }

        modeloDatos.getCaracteristicaService().save(c);
        return "Característica creada";
    }

    @GetMapping("/reportes/pdf")
    public ResponseEntity<?> reportePdf(@RequestParam int mes, @RequestParam int anio) {
        if (!esAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

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

    private List<Caracteristica> construirRuta(Caracteristica actual) {
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica cursor = actual;
        while (cursor != null) {
            ruta.add(0, cursor);
            cursor = cursor.getPadre();
        }
        return ruta;
    }

    private boolean esAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims)) return false;
        return Rol.ADMIN.name().equals(claims.get("rol", String.class));
    }
}
