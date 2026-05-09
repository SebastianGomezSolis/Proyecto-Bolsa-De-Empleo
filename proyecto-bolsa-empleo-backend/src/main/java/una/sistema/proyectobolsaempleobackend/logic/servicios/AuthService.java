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

    public LoginResponse login(LoginRequest request) {
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
        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol(), referenciaId);

        return new LoginResponse(usuario.getId(), usuario.getCorreo(), usuario.getRol().name(), referenciaId, token);
    }

    public void logout() {
        sesionUsuarioBean.logout();
    }

    private Integer resolverReferenciaId(Usuario usuario) {
        return switch (usuario.getRol()) {
            case ADMIN -> administradorRepository.findByUsuario(usuario)
                    .map(Administrador::getId).orElse(null);
            case EMPRESA -> empresaRepository.findByUsuario(usuario)
                    .map(Empresa::getId).orElse(null);
            case OFERENTE -> oferenteRepository.findByUsuario(usuario)
                    .map(Oferente::getId).orElse(null);
        };
    }
}
