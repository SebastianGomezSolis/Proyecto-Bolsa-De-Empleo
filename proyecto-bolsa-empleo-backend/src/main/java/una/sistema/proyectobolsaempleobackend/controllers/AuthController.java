package una.sistema.proyectobolsaempleobackend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;
import una.sistema.proyectobolsaempleobackend.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private ModeloDatos modeloDatos;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Usuario usuario = modeloDatos.getAuthService().login(request);
        if (usuario == null) {
            throw new RuntimeException("Credenciales inválidas");
        }
        Integer referenciaId = modeloDatos.getAuthService().resolverReferenciaId(usuario);
        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol(), referenciaId);
        return new LoginResponse(usuario.getId(), usuario.getCorreo(), usuario.getRol().name(), referenciaId, token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Sesión cerrada");
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
        return ResponseEntity.ok("Registro exitoso. Espere la aprobación del administrador.");
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
        return ResponseEntity.ok("Registro exitoso. Espere la aprobación del administrador.");
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> sesion() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Claims claims)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No hay sesión activa"));
        }
        return ResponseEntity.ok(new SesionResponse(
                claims.get("id", Integer.class),
                claims.getSubject(),
                claims.get("rol", String.class),
                claims.get("referenciaId", Integer.class)
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleRuntimeException(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }
}
