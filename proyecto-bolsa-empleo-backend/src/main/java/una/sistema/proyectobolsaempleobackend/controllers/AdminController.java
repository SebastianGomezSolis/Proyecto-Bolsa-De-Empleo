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

// Controller para la gestion de funciones administrativas.
// Proporciona endpoints para aprobar empresas y oferentes pendientes,
// administrar caracteristicas (arbol jerarquico), y generar reportes PDF.
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    // Acceso centralizado a todos los servicios del sistema
    @Autowired
    private ModeloDatos modeloDatos;

    // Bean de sesion para verificar el rol del usuario actual
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // Retorna la lista de empresas que estan pendientes de autorizacion.
    // Solo accesible para usuarios con rol ADMIN.
    @GetMapping("/empresas/pendientes")
    public ResponseEntity<?> empresasPendientes() {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        return ResponseEntity.ok(modeloDatos.getEmpresaService().findPendientes());
    }

    // Autoriza a una empresa para que pueda usar el sistema.
    // Activa su usuario y marca la empresa como autorizada.
    @PostMapping("/empresas/{id}/autorizar")
    public ResponseEntity<?> autorizarEmpresa(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        String error = modeloDatos.getEmpresaService().autorizar(id);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Empresa autorizada");
    }

    // Retorna la lista de oferentes pendientes de autorizacion.
    // Solo accesible para usuarios con rol ADMIN.
    @GetMapping("/oferentes/pendientes")
    public ResponseEntity<?> oferentesPendientes() {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        return ResponseEntity.ok(modeloDatos.getOferenteService().findPendientes());
    }

    // Autoriza a un oferente para que pueda usar el sistema.
    // Activa su usuario y marca el oferente como autorizado.
    @PostMapping("/oferentes/{id}/autorizar")
    public ResponseEntity<?> autorizarOferente(@PathVariable Integer id) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();
        String error = modeloDatos.getOferenteService().autorizar(id);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Oferente autorizado");
    }

    // Retorna el arbol de caracteristicas navegable.
    // Si no se pasa actualId, retorna las raices.
    // Si se pasa actualId, retorna los hijos de esa caracteristica.
    // Incluye la ruta de navegacion (padres) para breadcrumbs.
    @GetMapping("/caracteristicas")
    public ResponseEntity<?> caracteristicas(
            @RequestParam(required = false) Integer actualId) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        Map<String, Object> resp = new HashMap<>();

        // Si no hay ID, retornar raices del arbol
        if (actualId == null) {
            resp.put("subcategorias", modeloDatos.getCaracteristicaService().findRaices());
            resp.put("actual", null);
            resp.put("ruta", List.of());
        } else {
            // Buscar la caracteristica actual
            Caracteristica actual = modeloDatos.getCaracteristicaService().findById(actualId);
            if (actual == null) {
                // Si no existe, mostrar raices
                resp.put("subcategorias", modeloDatos.getCaracteristicaService().findRaices());
                resp.put("actual", null);
                resp.put("ruta", List.of());
            } else {
                // Mostrar hijos de la caracteristica actual
                resp.put("subcategorias", modeloDatos.getCaracteristicaService().findHijos(actualId));
                resp.put("actual", actual);
                resp.put("ruta", construirRuta(actual));
            }
        }

        // Incluir todas las caracteristicas para posibles selects
        resp.put("todas", modeloDatos.getCaracteristicaService().findAll());
        return ResponseEntity.ok(resp);
    }

    // Crea una nueva caracteristica en el arbol.
    // Valida que no exista otra con el mismo nombre en el mismo nivel.
    @PostMapping("/caracteristicas")
    public ResponseEntity<?> crearCaracteristica(@RequestBody Map<String, Object> body) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        // Obtener datos del request body
        String nombre  = (String) body.get("nombre");
        Integer padreId = body.get("padreId") != null ? (Integer) body.get("padreId") : null;

        // Validar que el nombre no sea vacio
        if (nombre == null || nombre.isBlank())
            return ResponseEntity.badRequest().body("El nombre de la característica es obligatorio");

        String nombreLimpio = nombre.trim();

        // Verificar si ya existe una caracteristica con ese nombre en el mismo nivel
        if (modeloDatos.getCaracteristicaService().existeEnMismoNivel(nombreLimpio, padreId))
            return ResponseEntity.badRequest()
                    .body("Ya existe una característica con ese nombre bajo el mismo padre");

        // Crear y configurar la nueva caracteristica
        Caracteristica c = new Caracteristica();
        c.setNombre(nombreLimpio);

        // Si hay padre, buscarlo y asignarlo
        if (padreId != null) {
            Caracteristica padre = modeloDatos.getCaracteristicaService().findById(padreId);
            if (padre == null) return ResponseEntity.badRequest().body("Padre no encontrado");
            c.setPadre(padre);
        }

        // Guardar en la base de datos
        modeloDatos.getCaracteristicaService().save(c);
        return ResponseEntity.ok("Característica creada");
    }

    // Genera un reporte PDF con los puestos publicados en un mes y ano especifico.
    // Retorna el archivo PDF con headers apropiados para descarga.
    @GetMapping("/reportes/pdf")
    public ResponseEntity<?> reportePdf(@RequestParam int mes, @RequestParam int anio) {
        if (!sesionUsuarioBean.isAdmin()) return forbidden();

        try {
            // Generar el PDF usando el servicio de reportes
            byte[] pdf = modeloDatos.getReporteService().generarPdfPuestosPorMesYAnio(mes, anio);

            // Configurar headers de la respuesta
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

    // Construye la ruta de navegacion desde la raiz hasta la caracteristica actual.
    // Se usa para mostrar breadcrumbs en el frontend.
    private List<Caracteristica> construirRuta(Caracteristica actual) {
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica cursor = actual;
        // Recorrer hacia arriba por los padres
        while (cursor != null) {
            ruta.add(0, cursor); // Agregar al inicio para mantener orden
            cursor = cursor.getPadre();
        }
        return ruta;
    }

    // Retorna una respuesta 403 Forbidden para solicitudes no autorizadas
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }
}
