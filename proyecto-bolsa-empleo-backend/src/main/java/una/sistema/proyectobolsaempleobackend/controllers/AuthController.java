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

// Controller REST para la autenticacion de usuarios.
// Proporciona endpoints para login, logout, registro de empresas y oferentes,
// y consulta del estado de la sesion actual.
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // Acceso centralizado a todos los servicios
    @Autowired
    private ModeloDatos modeloDatos;

    // Bean de sesion para almacenar info del usuario actual
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // POST /api/auth/login
    // Autentica a un usuario con correo y contrasena.
    // Retorna un LoginResponse con token JWT si las credenciales son validas.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse respuesta = modeloDatos.getAuthService().login(request);
        if (respuesta == null) {
            // Credenciales invalidas: retornar 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        return ResponseEntity.ok(respuesta);
    }

    // POST /api/auth/logout
    // Cierra la sesion del usuario actual.
    // Limpia el bean de sesion y retorna confirmacion.
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        modeloDatos.getAuthService().logout();
        return ResponseEntity.ok("Sesión cerrada");
    }

    // POST /api/auth/registro/empresa
    // Registra una nueva empresa en el sistema.
    // Las empresas requieren aprobacion de un administrador antes de poder iniciar sesion.
    @PostMapping("/registro/empresa")
    public ResponseEntity<?> registrarEmpresa(@RequestBody RegistroEmpresaRequest req) {
        // Crear entidad Usuario asociada
        Usuario usuario = new Usuario();
        usuario.setCorreo(req.getCorreo());
        usuario.setClave(req.getClave());

        // Crear entidad Empresa y configurar sus datos
        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setNombre(req.getNombre());
        empresa.setLocalizacion(req.getLocalizacion());
        empresa.setTelefono(req.getTelefono());
        empresa.setDescripcion(req.getDescripcion());

        // Registrar la empresa (validaciones y persistencia en el servicio)
        String error = modeloDatos.getEmpresaService().registrar(empresa);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Registro exitoso. Espere la aprobación del administrador.");
    }

    // POST /api/auth/registro/oferente
    // Registra un nuevo oferente (candidato) en el sistema.
    // Los oferentes requieren aprobacion de un administrador antes de poder iniciar sesion.
    @PostMapping("/registro/oferente")
    public ResponseEntity<?> registrarOferente(@RequestBody RegistroOferenteRequest req) {
        // Crear entidad Usuario asociada
        Usuario usuario = new Usuario();
        usuario.setCorreo(req.getCorreo());
        usuario.setClave(req.getClave());

        // Crear entidad Nacionalidad con el codigo ISO recibido
        Nacionalidad nac = new Nacionalidad();
        nac.setIso(req.getIsoNacionalidad());

        // Crear entidad Oferente y configurar sus datos
        Oferente oferente = new Oferente();
        oferente.setUsuario(usuario);
        oferente.setIdentificacion(req.getIdentificacion());
        oferente.setNombre(req.getNombre());
        oferente.setPrimerApellido(req.getPrimerApellido());
        oferente.setNacionalidad(nac);
        oferente.setTelefono(req.getTelefono());
        oferente.setLugarResidencia(req.getLugarResidencia());

        // Registrar el oferente (validaciones y persistencia en el servicio)
        String error = modeloDatos.getOferenteService().registrar(oferente);
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok("Registro exitoso. Espere la aprobación del administrador.");
    }

    // GET /api/auth/sesion
    // Retorna la informacion del usuario actualmente logueado.
    // Se usa para verificar estado de sesion en el frontend.
    @GetMapping("/sesion")
    public ResponseEntity<?> sesion() {
        if (!sesionUsuarioBean.isLogueado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay sesión activa");
        }
        // Retornar datos del usuario en sesion
        return ResponseEntity.ok(Map.of(
                "id", sesionUsuarioBean.getId(),
                "correo", sesionUsuarioBean.getCorreo(),
                "rol", sesionUsuarioBean.getRol().name(),
                "referenciaId", sesionUsuarioBean.getReferenciaId()
        ));
    }
}
