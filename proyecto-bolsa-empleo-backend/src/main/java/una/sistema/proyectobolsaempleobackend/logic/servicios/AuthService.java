package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.AdministradorRepository;
import una.sistema.proyectobolsaempleobackend.data.EmpresaRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.dto.LoginRequest;
import una.sistema.proyectobolsaempleobackend.logic.model.*;
import una.sistema.proyectobolsaempleobackend.security.JwtService;

// Servicio que maneja la autenticacion de usuarios en el sistema.
// Procesa solicitudes de login/logout y genera tokens JWT para sesiones.
@Service
public class AuthService {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordHash passwordHash;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    // Procesa la solicitud de inicio de sesion.
    // Valida credenciales, verifica que el usuario este activo,
    // y retorna el Usuario si es valido, o null si las credenciales son incorrectas.
    public Usuario login(LoginRequest request) {
        if (request.getCorreo() == null || request.getClave() == null) {
            return null;
        }

        Usuario usuario = usuarioService.findByCorreo(request.getCorreo());
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            return null;
        }

        if (!passwordHash.verify(request.getClave(), usuario.getClave())) {
            return null;
        }

        Integer referenciaId = resolverReferenciaId(usuario);
        sesionUsuarioBean.login(usuario.getId(), usuario.getCorreo(), usuario.getRol(), referenciaId);

        return usuario;
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
