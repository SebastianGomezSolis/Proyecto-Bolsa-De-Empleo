package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oferente")
public class OferenteController {
    @Autowired
    private ModeloDatos modeloDatos;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // ─── Perfil ────────────────────────────────────────────────────────────
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        if (!sesionUsuarioBean.isOferente()) return forbidden();
        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(oferente);
    }

    // ─── Habilidades ───────────────────────────────────────────────────────
    @GetMapping("/habilidades")
    public ResponseEntity<?> habilidades() {
        if (!sesionUsuarioBean.isOferente()) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getHabilidadService().findByOferente(sesionUsuarioBean.getReferenciaId()));
    }

    @PostMapping("/habilidades")
    public ResponseEntity<?> agregarHabilidad(@RequestBody Map<String, Object> body) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Integer caracteristicaId = (Integer) body.get("caracteristicaId");
        Integer nivel = (Integer) body.get("nivel");

        if (caracteristicaId == null || nivel == null)
            return ResponseEntity.badRequest().body("Datos incompletos");
        if (nivel < 1 || nivel > 5)
            return ResponseEntity.badRequest().body("El nivel debe ser entre 1 y 5");

        Caracteristica caracteristica = modeloDatos.getCaracteristicaService().findById(caracteristicaId);
        if (caracteristica == null)
            return ResponseEntity.badRequest().body("Caracter\u00edstica no encontrada");

        if (!modeloDatos.getCaracteristicaService().isHoja(caracteristicaId))
            return ResponseEntity.badRequest().body("Solo se pueden registrar habilidades de nivel hoja");

        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null)
            return ResponseEntity.badRequest().body("Oferente no encontrado");

        List<Habilidad> existentes = modeloDatos.getHabilidadService()
                .findByOferente(sesionUsuarioBean.getReferenciaId());
        for (Habilidad h : existentes) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId().equals(caracteristicaId)) {
                return ResponseEntity.badRequest()
                        .body("La habilidad \"" + caracteristica.getNombre() + "\" ya est\u00e1 registrada");
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
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Habilidad habilidad = modeloDatos.getHabilidadService().findById(id);
        if (habilidad == null) return ResponseEntity.notFound().build();

        if (!habilidad.getOferente().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        modeloDatos.getHabilidadService().deleteById(id);
        return ResponseEntity.ok("Habilidad eliminada");
    }

    // ─── Arbol de caracteristicas ──────────────────────────────────────────
    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(@RequestParam(required = false) Integer actualId) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Map<String, Object> resp = new HashMap<>();
        if (actualId == null) {
            resp.put("subcategorias", modeloDatos.getCaracteristicaService().findRaices());
            resp.put("actual", null);
        } else {
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) return ResponseEntity.notFound().build();
            resp.put("subcategorias", modeloDatos.getCaracteristicaService().findHijos(actualId));
            resp.put("actual", actual);
        }
        return ResponseEntity.ok(resp);
    }

    // ─── Busqueda de puestos ───────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPorCaracteristicas(
            @RequestParam(required = false) List<Integer> caracteristicaIds) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        List<Puesto> puestos;
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            puestos = List.of();
        } else {
            puestos = (List<Puesto>) modeloDatos.getPuestoService().findActivosAmbostiposPorCaracteristicas(caracteristicaIds);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(puestos));
        resp.put("raices", modeloDatos.getCaracteristicaService().findRaices());
        resp.put("tipoCambio", obtenerTipoCambio());
        resp.put("caracteristicaIds", caracteristicaIds);
        return ResponseEntity.ok(resp);
    }

    // ─── Detalle de puesto ──────────────────────────────────────────────────
    @GetMapping("/puestos/{id}")
    public ResponseEntity<?> detallePuesto(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo())
            return ResponseEntity.notFound().build();

        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        Map<String, Object> resp = new HashMap<>();
        resp.put("puesto", puesto);
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    // ─── Curriculum (CV) ───────────────────────────────────────────────────
    @PostMapping("/cv/subir")
    public ResponseEntity<?> subirCv(@RequestParam("archivo") MultipartFile archivo) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        if (archivo == null || archivo.isEmpty())
            return ResponseEntity.badRequest().body("Debe seleccionar un archivo PDF");

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".pdf"))
            return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");

        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null) return ResponseEntity.badRequest().body("Oferente no encontrado");

        try {
            Path directorio = buscarDirPublicFrontend().resolve("uploads/curriculos");
            Files.createDirectories(directorio);

            String idSanitizado = oferente.getIdentificacion().replaceAll("[^a-zA-Z0-9_-]", "_");
            File destino = new File(directorio.toFile(), idSanitizado + ".pdf");

            String canonicalDir = directorio.toFile().getCanonicalPath() + File.separator;
            if (!destino.getCanonicalPath().startsWith(canonicalDir))
                return ResponseEntity.badRequest().body("Ruta de archivo no permitida");

            archivo.transferTo(destino);

            String rutaRelativa = "uploads/curriculos/" + idSanitizado + ".pdf";
            modeloDatos.getOferenteService()
                    .actualizarCurriculum(sesionUsuarioBean.getReferenciaId(), rutaRelativa);

            return ResponseEntity.ok(Map.of("ruta", rutaRelativa));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
        }
    }

    // ─── Utilidades ────────────────────────────────────────────────────────
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
