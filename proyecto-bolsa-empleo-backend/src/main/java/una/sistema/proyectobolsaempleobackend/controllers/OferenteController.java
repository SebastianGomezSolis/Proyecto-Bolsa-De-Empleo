package una.sistema.proyectobolsaempleobackend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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

    @Value("${app.cv.upload-dir}")
    private String cvUploadDir;

    private Path cvBaseDir;

    @GetMapping("/perfil")
    public Oferente perfil() {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        Oferente oferente = modeloDatos.getOferenteService().findById(claims.get("referenciaId", Integer.class));
        if (oferente == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferente no encontrado");
        return oferente;
    }

    @GetMapping("/habilidades")
    public List habilidades() {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return modeloDatos.getHabilidadService().findByOferente(claims.get("referenciaId", Integer.class));
    }

    @PostMapping("/habilidades")
    public String agregarHabilidad(@RequestBody AgregarHabilidadRequest req) {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Integer caracteristicaId = req.getCaracteristicaId();
        Integer nivel = req.getNivel();

        if (caracteristicaId == null || nivel == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        if (nivel < 1 || nivel > 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nivel debe ser entre 1 y 5");

        Caracteristica caracteristica = modeloDatos.getCaracteristicaService().findById(caracteristicaId);
        if (caracteristica == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Característica no encontrada");

        if (!modeloDatos.getCaracteristicaService().isHoja(caracteristicaId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden registrar habilidades de nivel hoja");

        Integer refId = claims.get("referenciaId", Integer.class);
        Oferente oferente = modeloDatos.getOferenteService().findById(refId);
        if (oferente == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oferente no encontrado");

        List<Habilidad> existentes = modeloDatos.getHabilidadService().findByOferente(refId);
        for (Habilidad h : existentes) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId().equals(caracteristicaId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La habilidad \"" + caracteristica.getNombre() + "\" ya está registrada");
            }
        }

        Habilidad habilidad = new Habilidad();
        habilidad.setOferente(oferente);
        habilidad.setCaracteristica(caracteristica);
        habilidad.setNivel(nivel);
        modeloDatos.getHabilidadService().save(habilidad);
        return "Habilidad agregada";
    }

    @DeleteMapping("/habilidades/{id}")
    public String eliminarHabilidad(@PathVariable Integer id) {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Habilidad habilidad = modeloDatos.getHabilidadService().findById(id);
        if (habilidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidad no encontrada");

        if (!habilidad.getOferente().getId().equals(claims.get("referenciaId", Integer.class)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        modeloDatos.getHabilidadService().deleteById(id);
        return "Habilidad eliminada";
    }

    @GetMapping("/caracteristicas")
    public CaracteristicasOferenteResponse caracteristicas(@RequestParam(required = false) Integer actualId) {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        CaracteristicasOferenteResponse resp = new CaracteristicasOferenteResponse();
        if (actualId == null) {
            resp.setSubcategorias(modeloDatos.getCaracteristicaService().findRaices());
            resp.setActual(null);
        } else {
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Característica no encontrada");
            resp.setSubcategorias(modeloDatos.getCaracteristicaService().findHijos(actualId));
            resp.setActual(actual);
        }
        return resp;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/puestos/buscar")
    public BuscarPuestosResponse buscarPorCaracteristicas(
            @RequestParam(required = false) List<Integer> caracteristicaIds) {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            puestos = List.of();
        } else {
            puestos = (List<Puesto>) modeloDatos.getPuestoService().findActivosAmbostiposPorCaracteristicas(caracteristicaIds);
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
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado");

        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        return new DetallePuestoResponse(puesto, obtenerTipoCambio());
    }

    @GetMapping("/cv")
    public ResponseEntity<?> verCv() throws Exception {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        Oferente oferente = modeloDatos.getOferenteService().findById(claims.get("referenciaId", Integer.class));
        if (oferente == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferente no encontrado");

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV no encontrado");

        String raw = filename.replace("\\", "/");
        if (raw.contains("/")) raw = raw.substring(raw.lastIndexOf("/") + 1);

        Path filePath = cvBaseDir.resolve(raw).normalize();
        if (!filePath.startsWith(cvBaseDir))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta inválida");

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV no encontrado");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/cv/subir")
    public CvUploadResponse subirCv(@RequestParam("archivo") MultipartFile archivo) {
        Claims claims = getClaims();
        if (!Rol.OFERENTE.name().equals(claims.get("rol", String.class))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");

        if (archivo == null || archivo.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un archivo PDF");

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".pdf"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten archivos PDF");

        Integer refId = claims.get("referenciaId", Integer.class);
        Oferente oferente = modeloDatos.getOferenteService().findById(refId);
        if (oferente == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oferente no encontrado");

        try {
            Files.createDirectories(cvBaseDir);

            String idSanitizado = oferente.getIdentificacion().replaceAll("[^a-zA-Z0-9_-]", "_");
            File destino = new File(cvBaseDir.toFile(), idSanitizado + ".pdf");

            String canonicalDir = cvBaseDir.toFile().getCanonicalPath() + File.separator;
            if (!destino.getCanonicalPath().startsWith(canonicalDir))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta de archivo no permitida");

            archivo.transferTo(destino);

            modeloDatos.getOferenteService().actualizarCurriculum(refId, idSanitizado + ".pdf");

            return new CvUploadResponse(idSanitizado + ".pdf");

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el archivo");
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
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        return claims;
    }

    @PostConstruct
    public void init() {
        Path configurada = Paths.get(cvUploadDir);
        if (!configurada.isAbsolute()) {
            configurada = Paths.get(System.getProperty("user.dir"), cvUploadDir).normalize();
        }
        try {
            Files.createDirectories(configurada);
            cvBaseDir = configurada;
            return;
        } catch (Exception ignored) {}
        cvBaseDir = Paths.get(System.getProperty("user.dir"), "uploads", "curriculos").normalize();
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
