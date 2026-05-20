package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.AdministradorRepository;
import una.sistema.proyectobolsaempleobackend.data.EmpresaRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.dto.LoginRequest;
import una.sistema.proyectobolsaempleobackend.logic.model.*;

@Service
public class AuthService {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordHash passwordHash;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

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

        return usuario;
    }

    public Integer resolverReferenciaId(Usuario usuario) {
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
