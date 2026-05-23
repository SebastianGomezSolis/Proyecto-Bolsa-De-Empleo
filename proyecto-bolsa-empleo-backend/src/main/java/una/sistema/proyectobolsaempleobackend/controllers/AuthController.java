package una.sistema.proyectobolsaempleobackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import una.sistema.proyectobolsaempleobackend.dto.*;
import una.sistema.proyectobolsaempleobackend.logic.ModeloDatos;
import una.sistema.proyectobolsaempleobackend.logic.model.*;
import una.sistema.proyectobolsaempleobackend.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ModeloDatos modeloDatos;
    private final JwtService jwtService;

    public AuthController(ModeloDatos modeloDatos, JwtService jwtService) {
        this.modeloDatos = modeloDatos;
        this.jwtService = jwtService;
    }

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
    public String logout() {
        return "Sesión cerrada";
    }

    @PostMapping("/registro/empresa")
    public String registrarEmpresa(@RequestBody RegistroEmpresaRequest req) {
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
        if (error != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        return "Registro exitoso. Espere la aprobación del administrador.";
    }

    @PostMapping("/registro/oferente")
    public String registrarOferente(@RequestBody RegistroOferenteRequest req) {
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
        if (error != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        return "Registro exitoso. Espere la aprobación del administrador.";
    }

    @GetMapping("/sesion")
    public SesionResponse sesion(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión activa");
        }
        try {
            String token = authHeader.substring(7);
            var claims = jwtService.parsearClaims(token);
            return new SesionResponse(
                    claims.get("id", Integer.class),
                    claims.getSubject(),
                    claims.get("rol", String.class),
                    claims.get("referenciaId", Integer.class)
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }
}
