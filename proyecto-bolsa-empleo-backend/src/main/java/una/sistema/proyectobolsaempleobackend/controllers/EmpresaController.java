package una.sistema.proyectobolsaempleobackend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    @Autowired
    private ModeloDatos modeloDatos;

    private Path cvBaseDir;

    @GetMapping("/perfil")
    public Empresa perfil() {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Empresa empresa = modeloDatos.getEmpresaService().findById(claims.get("referenciaId", Integer.class));
        if (empresa == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada");

        return empresa;
    }

    @GetMapping("/puestos")
    public List puestos() {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return modeloDatos.getPuestoService().findByEmpresa(claims.get("referenciaId", Integer.class));
    }

    @PostMapping("/puestos")
    public String crearPuesto(@RequestBody CrearPuestoRequest req) {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Empresa empresa = modeloDatos.getEmpresaService().findById(claims.get("referenciaId", Integer.class));
        if (empresa == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa no encontrada");

        Puesto puesto = new Puesto();
        puesto.setDescripcion(req.getDescripcion());
        puesto.setSalario(req.getSalario());
        puesto.setTipoPublicacion(req.getTipoPublicacion());
        puesto.setEmpresa(empresa);

        try {
            modeloDatos.getPuestoService().crearConCaracteristicas(puesto, req.getCaracteristicaIds(), req.getNiveles());
            return "Puesto creado";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/puestos/{id}/desactivar")
    public String desactivarPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado");

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Puesto desactivado = modeloDatos.getPuestoService().desactivar(id);
        if (desactivado == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Puesto no encontrado");
        return "Puesto desactivado";
    }

    @PostMapping("/puestos/{id}/activar")
    public String activarPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado");

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        modeloDatos.getPuestoService().activar(id);
        return "Puesto activado";
    }

    @GetMapping("/puestos/{id}/candidatos")
    public List candidatosPorPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado");

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        return modeloDatos.getMatchingService().buscarCandidatosPorPuesto(id);
    }

    @GetMapping("/candidatos/{id}")
    public DetalleCandidatoResponse detalleCandidato(@PathVariable Integer id,
                                              @RequestParam Integer puestoId) {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferente no encontrado");

        Puesto puesto = modeloDatos.getPuestoService().findById(puestoId);

        return new DetalleCandidatoResponse(
                oferente,
                modeloDatos.getHabilidadService().findByOferente(id),
                puesto != null ? puesto : Map.of()
        );
    }

    @GetMapping("/candidatos/{id}/cv")
    public ResponseEntity<?> verCvCandidato(@PathVariable Integer id,
                                            @RequestParam(required = false) Integer puestoId) throws Exception {
        Claims claims = getClaims();
        if (!Rol.EMPRESA.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferente no encontrado");

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV no encontrado");

        String raw = filename.replace("\\", "/");
        if (raw.contains("/")) raw = raw.substring(raw.lastIndexOf("/") + 1);

        Path filePath = cvBaseDir.resolve(raw).normalize();
        if (!filePath.startsWith(cvBaseDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta inválida");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV no encontrado");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostConstruct
    public void init() {
        String wd = System.getProperty("user.dir");
        Path p1 = Paths.get(wd, "..", "proyecto-bolsa-empleo-frontend", "public", "uploads", "curriculos").normalize();
        Path p2 = Paths.get(wd, "proyecto-bolsa-empleo-frontend", "public", "uploads", "curriculos").normalize();
        for (Path p : new Path[]{p1, p2}) {
            Path frontendDir = p.getParent().getParent().getParent();
            if (frontendDir != null && Files.exists(frontendDir.resolve("package.json"))) {
                try {
                    Files.createDirectories(p);
                    cvBaseDir = p;
                    return;
                } catch (Exception ignored) {}
            }
        }
        cvBaseDir = Paths.get(wd, "uploads", "curriculos").normalize();
        try {
            Files.createDirectories(cvBaseDir);
        } catch (Exception e) {
            cvBaseDir = Paths.get(System.getProperty("user.home"), ".bolsa-empleo", "curriculos");
            try {
                Files.createDirectories(cvBaseDir);
            } catch (Exception ex) {
                throw new RuntimeException("No se pudo crear el directorio de CVs: " + ex.getMessage());
            }
        }
    }

    private Claims getClaims() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return claims;
    }
}
