package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controller para la gestion de oferentes.
// Proporciona endpoints para perfil, habilidades, busqueda de puestos y subida de curriculum (CV).
@RestController
@RequestMapping("/api/oferente")
public class OferenteController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // Bean de sesion para verificar permisos
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // Directorio base para almacenar CVs subidos (mismo que en EmpresaController)
    private Path cvBaseDir;

    // Retorna los datos del perfil del oferente logueado.
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        if (!sesionUsuarioBean.isOferente()) return forbidden();
        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(oferente);
    }

    // Retorna todas las habilidades registradas por el oferente logueado.
    @GetMapping("/habilidades")
    public ResponseEntity<?> habilidades() {
        if (!sesionUsuarioBean.isOferente()) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getHabilidadService().findByOferente(sesionUsuarioBean.getReferenciaId()));
    }

    // Agrega una nueva habilidad al perfil del oferente.
    // Valida que la caracteristica sea una hoja y que no este duplicada.
    @PostMapping("/habilidades")
    public ResponseEntity<?> agregarHabilidad(@RequestBody Map<String, Object> body) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Integer caracteristicaId = (Integer) body.get("caracteristicaId");
        Integer nivel = (Integer) body.get("nivel");

        // Validar datos basicos
        if (caracteristicaId == null || nivel == null)
            return ResponseEntity.badRequest().body("Datos incompletos");
        if (nivel < 1 || nivel > 5)
            return ResponseEntity.badRequest().body("El nivel debe ser entre 1 y 5");

        // Validar que la caracteristica exista
        Caracteristica caracteristica = modeloDatos.getCaracteristicaService().findById(caracteristicaId);
        if (caracteristica == null)
            return ResponseEntity.badRequest().body("Característica no encontrada");

        // Validar que sea una hoja (caracteristica final)
        if (!modeloDatos.getCaracteristicaService().isHoja(caracteristicaId))
            return ResponseEntity.badRequest().body("Solo se pueden registrar habilidades de nivel hoja");

        // Obtener el oferente
        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null)
            return ResponseEntity.badRequest().body("Oferente no encontrado");

        // Validar que no este duplicada la habilidad
        List<Habilidad> existentes = modeloDatos.getHabilidadService()
                .findByOferente(sesionUsuarioBean.getReferenciaId());
        for (Habilidad h : existentes) {
            if (h.getCaracteristica() != null && h.getCaracteristica().getId().equals(caracteristicaId)) {
                return ResponseEntity.badRequest()
                        .body("La habilidad \"" + caracteristica.getNombre() + "\" ya está registrada");
            }
        }

        // Crear y guardar la habilidad
        Habilidad habilidad = new Habilidad();
        habilidad.setOferente(oferente);
        habilidad.setCaracteristica(caracteristica);
        habilidad.setNivel(nivel);
        modeloDatos.getHabilidadService().save(habilidad);
        return ResponseEntity.ok("Habilidad agregada");
    }

    // Elimina una habilidad del perfil del oferente.
    // Verifica que la habilidad pertenezca al oferente logueado.
    @DeleteMapping("/habilidades/{id}")
    public ResponseEntity<?> eliminarHabilidad(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Habilidad habilidad = modeloDatos.getHabilidadService().findById(id);
        if (habilidad == null) return ResponseEntity.notFound().build();

        // Verificar propiedad de la habilidad
        if (!habilidad.getOferente().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        modeloDatos.getHabilidadService().deleteById(id);
        return ResponseEntity.ok("Habilidad eliminada");
    }

    // Retorna el arbol de caracteristicas navegable para que el oferente
    // seleccione habilidades. Solo incluye hojas en los niveles bajos.
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

    // Busca puestos (publicos y privados) que coincidan con las caracteristicas seleccionadas.
    // Retorna los puestos enriquecidos con sus datos y el tipo de cambio.
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

        // Construir respuesta con puestos, raices del arbol, tipo de cambio y filtros activos
        Map<String, Object> resp = new HashMap<>();
        resp.put("puestos", enriquecerPuestos(puestos));
        resp.put("raices", modeloDatos.getCaracteristicaService().findRaices());
        resp.put("tipoCambio", obtenerTipoCambio());
        resp.put("caracteristicaIds", caracteristicaIds);
        return ResponseEntity.ok(resp);
    }

    // Retorna el detalle completo de un puesto especifico.
    @GetMapping("/puestos/{id}")
    public ResponseEntity<?> detallePuesto(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null || !puesto.getActivo())
            return ResponseEntity.notFound().build();

        // Cargar caracteristicas del puesto
        puesto.setCaracteristicas(modeloDatos.getPuestoCaracteristicaService().findByPuesto(id));

        Map<String, Object> resp = new HashMap<>();
        resp.put("puesto", puesto);
        resp.put("tipoCambio", obtenerTipoCambio());
        return ResponseEntity.ok(resp);
    }

    // Descarga el CV del oferente logueado.
    @GetMapping("/cv")
    public ResponseEntity<?> verCv() throws Exception {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
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

    // Sube el curriculum (CV) del oferente en formato PDF.
    // El archivo se guarda en uploads/curriculos del frontend.
    @PostMapping("/cv/subir")
    public ResponseEntity<?> subirCv(@RequestParam("archivo") MultipartFile archivo) {
        if (!sesionUsuarioBean.isOferente()) return forbidden();

        // Validar que se haya enviado un archivo
        if (archivo == null || archivo.isEmpty())
            return ResponseEntity.badRequest().body("Debe seleccionar un archivo PDF");

        // Validar que sea PDF
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".pdf"))
            return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");

        Oferente oferente = modeloDatos.getOferenteService().findById(sesionUsuarioBean.getReferenciaId());
        if (oferente == null) return ResponseEntity.badRequest().body("Oferente no encontrado");

        try {
            // Crear directorio si no existe
            Files.createDirectories(cvBaseDir);

            // Nombre del archivo basado en identificacion del oferente
            String idSanitizado = oferente.getIdentificacion().replaceAll("[^a-zA-Z0-9_-]", "_");
            File destino = new File(cvBaseDir.toFile(), idSanitizado + ".pdf");

            // Validar ruta de destino para seguridad
            String canonicalDir = cvBaseDir.toFile().getCanonicalPath() + File.separator;
            if (!destino.getCanonicalPath().startsWith(canonicalDir))
                return ResponseEntity.badRequest().body("Ruta de archivo no permitida");

            // Guardar archivo
            archivo.transferTo(destino);

            // Guardar solo el nombre del archivo en la base de datos
            modeloDatos.getOferenteService()
                    .actualizarCurriculum(sesionUsuarioBean.getReferenciaId(), idSanitizado + ".pdf");

            return ResponseEntity.ok(Map.of("ruta", idSanitizado + ".pdf"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
        }
    }

    // Agrega las caracteristicas a cada puesto para enviar al frontend
    private List<Puesto> enriquecerPuestos(List<Puesto> puestos) {
        puestos.forEach(p -> p.setCaracteristicas(
                modeloDatos.getPuestoCaracteristicaService().findByPuesto(p.getId())));
        return puestos;
    }

    // Obtiene el tipo de cambio del dolar, manejando errores gracefully
    private Object obtenerTipoCambio() {
        try {
            return modeloDatos.getTipoCambioServicio().obtenerTipoCambio();
        } catch (Exception e) {
            return null;
        }
    }

    // Retorna respuesta 403 Forbidden
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }

    // Inicializa el directorio de CVs al arrancar la aplicacion
    @PostConstruct
    public void init() {
        String wd = System.getProperty("user.dir");
        // Opcion 1: desde el directorio del backend (../proyecto-bolsa-empleo-frontend)
        Path p1 = Paths.get(wd, "..", "proyecto-bolsa-empleo-frontend", "public", "uploads", "curriculos").normalize();
        // Opcion 2: desde el directorio padre (proyecto-bolsa-empleo-frontend)
        Path p2 = Paths.get(wd, "proyecto-bolsa-empleo-frontend", "public", "uploads", "curriculos").normalize();
        // Validar cada una verificando que exista package.json en el frontend
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
        // Fallback: directorio local uploads/curriculos
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
