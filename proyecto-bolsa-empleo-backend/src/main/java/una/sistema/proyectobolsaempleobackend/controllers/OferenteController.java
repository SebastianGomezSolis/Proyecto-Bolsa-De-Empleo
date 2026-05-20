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
import org.springframework.web.multipart.MultipartFile;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/oferente")
public class OferenteController {
    @Autowired
    private ModeloDatos modeloDatos;

    private Path cvBaseDir;

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();
        Oferente oferente = modeloDatos.getOferenteService().findById(claims.get("referenciaId", Integer.class));
        if (oferente == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(oferente);
    }

    @GetMapping("/habilidades")
    public ResponseEntity<?> habilidades() {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getHabilidadService().findByOferente(claims.get("referenciaId", Integer.class)));
    }

    @PostMapping("/habilidades")
    public ResponseEntity<?> agregarHabilidad(@RequestBody AgregarHabilidadRequest req) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        Integer caracteristicaId = req.getCaracteristicaId();
        Integer nivel = req.getNivel();

        if (caracteristicaId == null || nivel == null)
            return ResponseEntity.badRequest().body("Datos incompletos");
        if (nivel < 1 || nivel > 5)
            return ResponseEntity.badRequest().body("El nivel debe ser entre 1 y 5");

        Caracteristica caracteristica = modeloDatos.getCaracteristicaService().findById(caracteristicaId);
        if (caracteristica == null)
            return ResponseEntity.badRequest().body("Característica no encontrada");

        if (!modeloDatos.getCaracteristicaService().isHoja(caracteristicaId))
            return ResponseEntity.badRequest().body("Solo se pueden registrar habilidades de nivel hoja");

        Integer refId = claims.get("referenciaId", Integer.class);
        Oferente oferente = modeloDatos.getOferenteService().findById(refId);
        if (oferente == null)
            return ResponseEntity.badRequest().body("Oferente no encontrado");

        List<Habilidad> existentes = modeloDatos.getHabilidadService().findByOferente(refId);
        for (Habilidad h : existentes) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId().equals(caracteristicaId)) {
                return ResponseEntity.badRequest()
                        .body("La habilidad \"" + caracteristica.getNombre() + "\" ya está registrada");
            }
        }

        Habilidad habilidad = new Habilidad();
        habilidad.setOferente(oferente);
        habilidad.setCaracteristica(caracteristica);
        habilidad.setNivel(nivel);
        modeloDatos.getHabilidadService().save(habilidad);
        return ResponseEntity.ok("Habilidad agregada");
    }

    @DeleteMapping("/habilidades/{id}")
    public ResponseEntity<?> eliminarHabilidad(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        Habilidad habilidad = modeloDatos.getHabilidadService().findById(id);
        if (habilidad == null) return ResponseEntity.notFound().build();

        if (!habilidad.getOferente().getId().equals(claims.get("referenciaId", Integer.class)))
            return forbidden();

        modeloDatos.getHabilidadService().deleteById(id);
        return ResponseEntity.ok("Habilidad eliminada");
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(@RequestParam(required = false) Integer actualId) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        CaracteristicasOferenteResponse resp = new CaracteristicasOferenteResponse();
        if (actualId == null) {
            resp.setSubcategorias(modeloDatos.getCaracteristicaService().findRaices());
            resp.setActual(null);
        } else {
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) return ResponseEntity.notFound().build();
            resp.setSubcategorias(modeloDatos.getCaracteristicaService().findHijos(actualId));
            resp.setActual(actual);
        }
        return ResponseEntity.ok(resp);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPorCaracteristicas(
            @RequestParam(required = false) List<Integer> caracteristicaIds) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            puestos = List.of();
        } else {
            puestos = (List<Puesto>) modeloDatos.getPuestoService().findActivosAmbostiposPorCaracteristicas(caracteristicaIds);
        }

        return ResponseEntity.ok(new BuscarPuestosResponse(
                enriquecerPuestos(puestos),
                modeloDatos.getCaracteristicaService().findRaices(),
                obtenerTipoCambio(),
                caracteristicaIds
        ));
    }

    @GetMapping("/puestos/{id}")
    public ResponseEntity<?> detallePuesto(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo())
            return ResponseEntity.notFound().build();

        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        return ResponseEntity.ok(new DetallePuestoResponse(puesto, obtenerTipoCambio()));
    }

    @GetMapping("/cv")
    public ResponseEntity<?> verCv() throws Exception {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(claims.get("referenciaId", Integer.class));
        if (oferente == null) return ResponseEntity.notFound().build();

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) return ResponseEntity.notFound().build();

        String raw = filename.replace("\\", "/");
        if (raw.contains("/")) raw = raw.substring(raw.lastIndexOf("/") + 1);

        Path filePath = cvBaseDir.resolve(raw).normalize();
        if (!filePath.startsWith(cvBaseDir))
            return ResponseEntity.status(400).body("Ruta inválida");

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/cv/subir")
    public ResponseEntity<?> subirCv(@RequestParam("archivo") MultipartFile archivo) {
        Claims claims = getClaims();
        if (claims == null || !Rol.OFERENTE.name().equals(claims.get("rol", String.class))) return forbidden();

        if (archivo == null || archivo.isEmpty())
            return ResponseEntity.badRequest().body("Debe seleccionar un archivo PDF");

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".pdf"))
            return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");

        Integer refId = claims.get("referenciaId", Integer.class);
        Oferente oferente = modeloDatos.getOferenteService().findById(refId);
        if (oferente == null) return ResponseEntity.badRequest().body("Oferente no encontrado");

        try {
            Files.createDirectories(cvBaseDir);

            String idSanitizado = oferente.getIdentificacion().replaceAll("[^a-zA-Z0-9_-]", "_");
            File destino = new File(cvBaseDir.toFile(), idSanitizado + ".pdf");

            String canonicalDir = cvBaseDir.toFile().getCanonicalPath() + File.separator;
            if (!destino.getCanonicalPath().startsWith(canonicalDir))
                return ResponseEntity.badRequest().body("Ruta de archivo no permitida");

            archivo.transferTo(destino);

            modeloDatos.getOferenteService().actualizarCurriculum(refId, idSanitizado + ".pdf");

            return ResponseEntity.ok(new CvUploadResponse(idSanitizado + ".pdf"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
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

    private Claims getClaims() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims)) return null;
        return claims;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
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
}
