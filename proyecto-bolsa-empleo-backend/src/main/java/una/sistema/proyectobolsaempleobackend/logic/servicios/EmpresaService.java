package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.EmpresaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;

@Service
public class EmpresaService {
    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioService usuarioService;

    public Iterable<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    public Empresa findById(Integer id) {
        return empresaRepository.findById(id).orElse(null);
    }

    public List<Empresa> findPendientes() {
        return empresaRepository.findByAutorizadoFalse();
    }

    public String registrar(Empresa empresa) {
        if (empresa.getUsuario() == null)
            return "El usuario es requerido";

        if (empresa.getUsuario().getCorreo() == null || empresa.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        if (empresa.getUsuario().getClave() == null || empresa.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        if (empresa.getNombre() == null || empresa.getNombre().isBlank())
            return "El nombre es requerido";

        if (usuarioService.existeCorreo(empresa.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        empresa.getUsuario().setRol(Rol.EMPRESA);
        empresa.getUsuario().setActivo(false);
        Usuario guardado = usuarioService.guardar(empresa.getUsuario());
        empresa.setUsuario(guardado);
        empresa.setAutorizado(false);
        empresaRepository.save(empresa);
        return null;
    }

    public String autorizar(Integer id) {
        Empresa empresa = findById(id);
        if (empresa == null) return "Empresa no encontrada";
        empresa.setAutorizado(true);
        empresaRepository.save(empresa);
        Usuario usuario = empresa.getUsuario();
        usuario.setActivo(true);
        usuarioService.guardarSinHash(usuario);
        return null;
    }
}
