package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.AdministradorRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

@Service
public class AdministradorService {
    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioService usuarioService;

    public Iterable<Administrador> findAll() {
        return administradorRepository.findAll();
    }

    public Administrador findById(Integer id) {
        return administradorRepository.findById(id).orElse(null);
    }

    public String crear(Administrador admin) {
        if (admin.getUsuario() == null)
            return "El usuario es requerido";

        if (admin.getUsuario().getCorreo() == null || admin.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        if (admin.getUsuario().getClave() == null || admin.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        if (admin.getIdentificacion() == null || admin.getIdentificacion().isBlank())
            return "La identificación es requerida";

        if (admin.getNombre() == null || admin.getNombre().isBlank())
            return "El nombre es requerido";

        if (usuarioService.existeCorreo(admin.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        admin.getUsuario().setRol(Rol.ADMIN);
        admin.getUsuario().setActivo(true);
        Usuario guardado = usuarioService.guardar(admin.getUsuario());
        admin.setUsuario(guardado);
        administradorRepository.save(admin);
        return null;
    }
}
