package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;
import una.sistema.proyectobolsaempleobackend.logic.model.SesionUsuarioBean;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

// Controller para la gestion de empresas.
// Proporciona endpoints para perfil de empresa, creacion y gestion de puestos,
// visualizacion de candidatos y descarga de CVs.
@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // Bean de sesion para verificar identidad y permisos
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // Directorio base para almacenar CVs subidos
    private Path cvBaseDir;

    // GET /api/empresa/perfil
    // Retorna los datos del perfil de la empresa actualmente logueada.
    // Solo accesible para usuarios con rol EMPRESA.
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil() {
        if (!sesionUsuarioBean.isEmpresa())
            return forbidden();

        // Buscar empresa por ID de referencia de la sesion
        Empresa empresa = modeloDatos.getEmpresaService().findById(sesionUsuarioBean.getReferenciaId());

        if (empresa == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(empresa);
    }

    // GET /api/empresa/puestos
    // Retorna todos los puestos belonging a la empresa logueada.
    @GetMapping("/puestos")
    public ResponseEntity<?> puestos() {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();
        return ResponseEntity.ok(
                modeloDatos.getPuestoService().findByEmpresa(sesionUsuarioBean.getReferenciaId()));
    }

    // POST /api/empresa/puestos
    // Crea un nuevo puesto de trabajo con sus caracteristicas y niveles requeridos.
    @PostMapping("/puestos")
    public ResponseEntity<?> crearPuesto(@RequestBody CrearPuestoRequest req) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        // Obtener la empresa logueada
        Empresa empresa = modeloDatos.getEmpresaService().findById(sesionUsuarioBean.getReferenciaId());
        if (empresa == null) return ResponseEntity.badRequest().body("Empresa no encontrada");

        // Crear entidad Puesto y configurar sus datos
        Puesto puesto = new Puesto();
        puesto.setDescripcion(req.getDescripcion());
        puesto.setSalario(req.getSalario());
        puesto.setTipoPublicacion(req.getTipoPublicacion());
        puesto.setEmpresa(empresa);

        try {
            // Crear puesto con sus caracteristicas (con transaccion)
            modeloDatos.getPuestoService().crearConCaracteristicas(puesto, req.getCaracteristicaIds(), req.getNiveles());
            return ResponseEntity.ok("Puesto creado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/empresa/puestos/{id}/desactivar
    // Desactiva un puesto para ocultarlo de las busquedas publicas.
    // Verifica que el puesto pertenezca a la empresa logueada.
    @PostMapping("/puestos/{id}/desactivar")
    public ResponseEntity<?> desactivarPuesto(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Puesto puesto = modeloDatos.getPuestoService().findById(id);
        if (puesto == null) return ResponseEntity.notFound().build();

        // Verificar que el puesto pertenece a la empresa en sesion
        if (puesto.getEmpresa() == null
                || !puesto.getEmpresa().getId().equals(sesionUsuarioBean.getReferenciaId()))
            return forbidden();

        Puesto desactivado = modeloDatos.getPuestoService().desactivar(id);
        if (desactivado == null) return ResponseEntity.badRequest().body("Puesto no encontrado");
        return ResponseEntity.ok("Puesto desactivado");
    }

    // POST /api/empresa/puestos/{id}/activar
    // Reactiva un puesto previamente desactivado.
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

    // GET /api/empresa/puestos/{id}/candidatos
    // Retorna la lista de candidatos compatibles con un puesto especifico.
    // Incluye ranking de similitud calculado por el MatchingService.
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

    // GET /api/empresa/candidatos/{id}
    // Retorna el detalle completo de un candidato especifico.
    // Incluye sus habilidades y datos del puesto asociado.
    @GetMapping("/candidatos/{id}")
    public ResponseEntity<?> detalleCandidato(@PathVariable Integer id,
                                              @RequestParam Integer puestoId) {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        Puesto puesto = modeloDatos.getPuestoService().findById(puestoId);

        // Construir respuesta con datos del oferente, habilidades y puesto
        DetalleCandidatoResponse resp = new DetalleCandidatoResponse();
        resp.setOferente(oferente);
        resp.setHabilidades(modeloDatos.getHabilidadService().findByOferente(id));
        resp.setPuesto(puesto != null ? puesto : Map.of());
        return ResponseEntity.ok(resp);
    }

    // GET /api/empresa/candidatos/{id}/cv
    // Descarga el CV (PDF) de un candidato especifico.
    // Busca el archivo en el directorio uploads/curriculos del frontend.
    @GetMapping("/candidatos/{id}/cv")
    public ResponseEntity<?> verCvCandidato(@PathVariable Integer id,
                                            @RequestParam(required = false) Integer puestoId) throws Exception {
        if (!sesionUsuarioBean.isEmpresa()) return forbidden();

        Oferente oferente = modeloDatos.getOferenteService().findById(id);
        if (oferente == null) return ResponseEntity.notFound().build();

        String filename = oferente.getCurriculum();
        if (filename == null || filename.isBlank()) return ResponseEntity.notFound().build();

        // Normalizar nombre del archivo
        String raw = filename.replace("\\", "/");
        if (raw.contains("/")) raw = raw.substring(raw.lastIndexOf("/") + 1);

        Path filePath = cvBaseDir.resolve(raw).normalize();

        // Validar que la ruta no salga del directorio base (seguridad)
        if (!filePath.startsWith(cvBaseDir)) {
            return ResponseEntity.status(400).body("Ruta inválida");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        // Retornar el PDF
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
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

    // Retorna respuesta 403 Forbidden
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }
}
