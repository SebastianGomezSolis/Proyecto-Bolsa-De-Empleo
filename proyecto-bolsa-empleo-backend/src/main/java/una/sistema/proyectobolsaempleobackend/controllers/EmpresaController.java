package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.CrearPuestoRequest;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;
import una.sistema.proyectobolsaempleobackend.logic.model.SesionUsuarioBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    @Autowired
    private ModeloDatos modeloDatos;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // ─── Perfil ────────────────────────────────────────────────────────────
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        if (!sesionUsuarioBean.isEmpresa())
            return forbidden();

        Empresa empresa = modeloDatos.getEmpresaService().findById(sesionUsuarioBean.getReferenciaId());

        if (empresa == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(empresa);
    }

    // ─── Puestos ───────────────────────────────────────────────────────────
    @GetMapping("/puestos")
    public ResponseEntity<?> puestos() {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getPuestoService().findByEmpresa(sesionUsuarioBean.getReferenciaId()));
    }

    @PostMapping("/puestos")
    public ResponseEntity<?> crearPuesto(@RequestBody CrearPuestoRequest req) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Empresa empresa = modeloDatos.getEmpresaService().findById(sesionUsuarioBean.getReferenciaId());
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
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        // Verificar que el puesto pertenece a la empresa en sesión
        if (puesto.getEmpresa() == null
                || !puesto.getEmpresa().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        String error = modeloDatos.getPuestoService().desactivar(id);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Puesto desactivado");
    }

    @PostMapping("/puestos/{id}/activar")
    public ResponseEntity<?> activarPuesto(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        if (puesto.getEmpresa() == null
                || !puesto.getEmpresa().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        modeloDatos.getPuestoService().activar(id);
        return ResponseEntity.ok("Puesto activado");
    }

    // ─── Candidatos ────────────────────────────────────────────────────────
    @GetMapping("/puestos/{id}/candidatos")
    public ResponseEntity<?> candidatosPorPuesto(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        if (puesto.getEmpresa() == null
                || !puesto.getEmpresa().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        return ResponseEntity.ok(modeloDatos.getMatchingService().buscarCandidatosPorPuesto(id));
    }

    @GetMapping("/candidatos/{id}")
    public ResponseEntity<?> detalleCandidato(@PathVariable Integer id,
                                              @RequestParam Integer puestoId) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        Puesto puesto = modeloDatos.getPuestoService().findById(puestoId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("oferente", oferente);
        resp.put("habilidades", modeloDatos.getHabilidadService().findByOferente(id));
        resp.put("puesto", puesto != null ? puesto : Map.of());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/candidatos/{id}/cv")
    public ResponseEntity<?> verCvCandidato(@PathVariable Integer id,
                                            @RequestParam(required = false) Integer puestoId) throws Exception {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) return ResponseEntity.notFound().build();

        String raw = filename.replace("\\", "/");
        if (raw.startsWith("/")) raw = raw.substring(1);

        Path baseDir = buscarDirPublicFrontend().resolve("uploads/curriculos").normalize();

        Path filePath;
        if (Paths.get(raw).isAbsolute()) {
            filePath = Paths.get(raw).toAbsolutePath().normalize();
        } else {
            if (raw.startsWith("/uploads/curriculos/")) raw = raw.substring("/uploads/curriculos/".length());
            if (raw.startsWith("uploads/curriculos/")) raw = raw.substring("uploads/curriculos/".length());
            filePath = baseDir.resolve(raw).normalize();
        }

        if (!filePath.startsWith(baseDir)) {
            return ResponseEntity.status(400).body("Ruta inválida");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    // ─── Utilidades ────────────────────────────────────────────────────────
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }

    private Path buscarDirPublicFrontend() {
        String nombreCarpeta = "proyecto-bolsa-empleo-frontend";
        Path actual = Paths.get("").toAbsolutePath().normalize();
        while (actual != null) {
            Path candidato = actual.resolve(nombreCarpeta).resolve("public");
            if (Files.isDirectory(candidato)) {
                return candidato.normalize();
            }
            actual = actual.getParent();
        }
        return Paths.get("").toAbsolutePath().resolve(nombreCarpeta).resolve("public").normalize();
    }
}
