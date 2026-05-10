package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.AdministradorRepository;
import una.sistema.proyectobolsaempleobackend.data.EmpresaRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.dto.LoginRequest;
import una.sistema.proyectobolsaempleobackend.dto.LoginResponse;
import una.sistema.proyectobolsaempleobackend.logic.model.*;
import una.sistema.proyectobolsaempleobackend.security.JwtService;

// Servicio que maneja la autenticacion de usuarios en el sistema.
// Procesa solicitudes de login/logout y genera tokens JWT para sesiones.
@Service
public class AuthService {
    // Servicio para gestionar usuarios (busqueda por correo, etc.)
    @Autowired
    private UsuarioService usuarioService;

    // Servicio para hashear y verificar contrasenas con BCrypt
    @Autowired
    private PasswordHash passwordHash;

    // Bean de sesion para almacenar info del usuario logueado
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // Servicio para generar y validar tokens JWT
    @Autowired
    private JwtService jwtService;

    // Repositorio para buscar administradores por usuario
    @Autowired
    private AdministradorRepository administradorRepository;

    // Repositorio para buscar empresas por usuario
    @Autowired
    private EmpresaRepository empresaRepository;

    // Repositorio para buscar oferentes por usuario
    @Autowired
    private OferenteRepository oferenteRepository;

    // Procesa la solicitud de inicio de sesion.
    // Valida credenciales, verifica que el usuario este activo,
    // y retorna un LoginResponse con token JWT si es exitoso.
    public LoginResponse login(LoginRequest request) {
        // Validacion basica: ambos campos deben estar presentes
        if (request.getCorreo() == null || request.getClave() == null) {
            return null;
        }

        // Buscar usuario por correo en la base de datos
        Usuario usuario = usuarioService.findByCorreo(request.getCorreo());
        // Verificar que el usuario exista y este activo (no deshabilitado)
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            return null;
        }

        // Verificar que la contrasena proporcionada coincida con el hash almacenado
        if (!passwordHash.verify(request.getClave(), usuario.getClave())) {
            return null;
        }

        // Obtener el ID de referencia segun el tipo de usuario
        // (Administrador.id, Empresa.id u Oferente.id)
        Integer referenciaId = resolverReferenciaId(usuario);

        // Almacenar info del usuario en la sesion HTTP
        sesionUsuarioBean.login(usuario.getId(), usuario.getCorreo(), usuario.getRol(), referenciaId);

        // Generar token JWT con la informacion del usuario
        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol(), referenciaId);

        // Retornar respuesta con datos del usuario y token
        return new LoginResponse(usuario.getId(), usuario.getCorreo(), usuario.getRol().name(), referenciaId, token);
    }

    // Cierra la sesion del usuario actual limpiando el bean de sesion
    public void logout() {
        sesionUsuarioBean.logout();
    }

    // Resuelve el ID de referencia segun el rol del usuario.
    // Busca en la tabla correspondiente (admin, empresa u oferente)
    // y retorna su ID unico para almacenarlo en el token JWT.
    private Integer resolverReferenciaId(Usuario usuario) {
        return switch (usuario.getRol()) {
            // Si es admin, buscar en Administrador y obtener su ID
            case ADMIN -> administradorRepository.findByUsuario(usuario)
                    .map(Administrador::getId).orElse(null);
            // Si es empresa, buscar en Empresa y obtener su ID
            case EMPRESA -> empresaRepository.findByUsuario(usuario)
                    .map(Empresa::getId).orElse(null);
            // Si es oferente, buscar en Oferente y obtener su ID
            case OFERENTE -> oferenteRepository.findByUsuario(usuario)
                    .map(Oferente::getId).orElse(null);
        };
    }
}
