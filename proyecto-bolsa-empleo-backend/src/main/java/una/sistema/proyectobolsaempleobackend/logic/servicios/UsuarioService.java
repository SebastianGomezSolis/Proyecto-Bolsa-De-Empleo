package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.UsuarioRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHash passwordHash;

    public Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public Usuario guardar(Usuario usuario) {
        usuario.setClave(passwordHash.hash(usuario.getClave()));
        return usuarioRepository.save(usuario);
    }

    public Usuario guardarSinHash(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
