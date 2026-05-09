package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.LoginRequest;
import una.sistema.proyectobolsaempleobackend.dto.LoginResponse;
import una.sistema.proyectobolsaempleobackend.dto.RegistroEmpresaRequest;
import una.sistema.proyectobolsaempleobackend.dto.RegistroOferenteRequest;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private ModeloDatos modeloDatos;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse respuesta = modeloDatos.getAuthService().login(request);
        if (respuesta == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inv\u00e1lidas");
        }
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        modeloDatos.getAuthService().logout();
        return ResponseEntity.ok("Sesi\u00f3n cerrada");
    }

    @PostMapping("/registro/empresa")
    public ResponseEntity<?> registrarEmpresa(@RequestBody RegistroEmpresaRequest req) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(req.getCorreo());
        usuario.setClave(req.getClave());

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setNombre(req.getNombre());
        empresa.setLocalizacion(req.getLocalizacion());
        empresa.setTelefono(req.getTelefono());
        empresa.setDescripcion(req.getDescripcion());

        String error = modeloDatos.getEmpresaService().registrar(empresa);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Registro exitoso. Espere la aprobaci\u00f3n del administrador.");
    }

    @PostMapping("/registro/oferente")
    public ResponseEntity<?> registrarOferente(@RequestBody RegistroOferenteRequest req) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(req.getCorreo());
        usuario.setClave(req.getClave());

        Nacionalidad nac = new Nacionalidad();
        nac.setIso(req.getIsoNacionalidad());

        Oferente oferente = new Oferente();
        oferente.setUsuario(usuario);
        oferente.setIdentificacion(req.getIdentificacion());
        oferente.setNombre(req.getNombre());
        oferente.setPrimerApellido(req.getPrimerApellido());
        oferente.setNacionalidad(nac);
        oferente.setTelefono(req.getTelefono());
        oferente.setLugarResidencia(req.getLugarResidencia());

        String error = modeloDatos.getOferenteService().registrar(oferente);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Registro exitoso. Espere la aprobaci\u00f3n del administrador.");
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> sesion() {
        if (!sesionUsuarioBean.isLogueado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay sesi\u00f3n activa");
        }
        return ResponseEntity.ok(Map.of(
                "id", sesionUsuarioBean.getId(),
                "correo", sesionUsuarioBean.getCorreo(),
                "rol", sesionUsuarioBean.getRol().name(),
                "referenciaId", sesionUsuarioBean.getReferenciaId()
        ));
    }
}
