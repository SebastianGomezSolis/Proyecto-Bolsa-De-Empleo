package una.sistema.proyectobolsaempleobackend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    @Autowired
    private ModeloDatos modeloDatos;

    private Path cvBaseDir;

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class)))
            return forbidden();

        Empresa empresa = modeloDatos.getEmpresaService().findById(claims.get("referenciaId", Integer.class));
        if (empresa == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(empresa);
    }

    @GetMapping("/puestos")
    public ResponseEntity<?> puestos() {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getPuestoService().findByEmpresa(claims.get("referenciaId", Integer.class)));
    }

    @PostMapping("/puestos")
    public ResponseEntity<?> crearPuesto(@RequestBody CrearPuestoRequest req) {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Empresa empresa = modeloDatos.getEmpresaService().findById(claims.get("referenciaId", Integer.class));
        if (empresa == null) return ResponseEntity.badRequest().body("Empresa no encontrada");

        Puesto puesto = new Puesto();
        puesto.setDescripcion(req.getDescripcion());
        puesto.setSalario(req.getSalario());
        puesto.setTipoPublicacion(req.getTipoPublicacion());
        puesto.setEmpresa(empresa);

        try {
            modeloDatos.getPuestoService().crearConCaracteristicas(puesto, req.getCaracteristicaIds(), req.getNiveles());
            return ResponseEntity.ok("Puesto creado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/puestos/{id}/desactivar")
    public ResponseEntity<?> desactivarPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            return forbidden();

        Puesto desactivado = modeloDatos.getPuestoService().desactivar(id);
        if (desactivado == null) return ResponseEntity.badRequest().body("Puesto no encontrado");
        return ResponseEntity.ok("Puesto desactivado");
    }

    @PostMapping("/puestos/{id}/activar")
    public ResponseEntity<?> activarPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            return forbidden();

        modeloDatos.getPuestoService().activar(id);
        return ResponseEntity.ok("Puesto activado");
    }

    @GetMapping("/puestos/{id}/candidatos")
    public ResponseEntity<?> candidatosPorPuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        Integer refId = claims.get("referenciaId", Integer.class);
        if (puesto.getEmpresa() == null || !puesto.getEmpresa().getId().equals(refId))
            return forbidden();

        return ResponseEntity.ok(modeloDatos.getMatchingService().buscarCandidatosPorPuesto(id));
    }

    @GetMapping("/candidatos/{id}")
    public ResponseEntity<?> detalleCandidato(@PathVariable Integer id,
                                              @RequestParam Integer puestoId) {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        Puesto puesto = modeloDatos.getPuestoService().findById(puestoId);

        return ResponseEntity.ok(new DetalleCandidatoResponse(
                oferente,
                modeloDatos.getHabilidadService().findByOferente(id),
                puesto != null ? puesto : Map.of()
        ));
    }

    @GetMapping("/candidatos/{id}/cv")
    public ResponseEntity<?> verCvCandidato(@PathVariable Integer id,
                                            @RequestParam(required = false) Integer puestoId) throws Exception {
        Claims claims = getClaims();
        if (claims == null || !Rol.EMPRESA.name().equals(claims.get("rol", String.class))) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) return ResponseEntity.notFound().build();

        String raw = filename.replace("\\", "/");
        if (raw.contains("/")) raw = raw.substring(raw.lastIndexOf("/") + 1);

        Path filePath = cvBaseDir.resolve(raw).normalize();
        if (!filePath.startsWith(cvBaseDir)) {
            return ResponseEntity.status(400).body("Ruta inválida");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

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
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims)) return null;
        return claims;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }
}
