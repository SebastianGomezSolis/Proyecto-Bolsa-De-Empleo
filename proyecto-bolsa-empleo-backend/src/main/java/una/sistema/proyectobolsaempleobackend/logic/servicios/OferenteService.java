package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.NacionalidadRepository;
import una.sistema.proyectobolsaempleobackend.data.OferenteRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;

@Service
public class OferenteService {
    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NacionalidadRepository nacionalidadRepository;

    public Iterable<Oferente> findAll() {
        return oferenteRepository.findAll();
    }

    public Oferente findById(Integer id) {
        return oferenteRepository.findById(id).orElse(null);
    }

    public List<Oferente> findPendientes() {
        return oferenteRepository.findByAutorizadoFalse();
    }

    public String registrar(Oferente oferente) {
        if (oferente.getUsuario() == null)
            return "El usuario es requerido";

        if (oferente.getUsuario().getCorreo() == null || oferente.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        if (oferente.getUsuario().getClave() == null || oferente.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        if (oferente.getIdentificacion() == null || oferente.getIdentificacion().isBlank())
            return "La identificación es requerida";

        if (oferente.getNombre() == null || oferente.getNombre().isBlank())
            return "El nombre es requerido";

        if (oferente.getPrimerApellido() == null || oferente.getPrimerApellido().isBlank())
            return "El primer apellido es requerido";

        if (usuarioService.existeCorreo(oferente.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        if (oferenteRepository.existsByIdentificacion(oferente.getIdentificacion()))
            return "La identificación ya está registrada";

        if (oferente.getNacionalidad() == null || oferente.getNacionalidad().getIso() == null)
            return "Nacionalidad inválida";

        Nacionalidad nac = nacionalidadRepository.findById(oferente.getNacionalidad().getIso()).orElse(null);

        if (nac == null)
            return "Nacionalidad inválida";

        oferente.getUsuario().setRol(Rol.OFERENTE);
        oferente.getUsuario().setActivo(false);
        Usuario guardado = usuarioService.guardar(oferente.getUsuario());
        oferente.setUsuario(guardado);
        oferente.setNacionalidad(nac);
        oferente.setAutorizado(false);
        oferenteRepository.save(oferente);
        return null;
    }

    public String autorizar(Integer id) {
        Oferente oferente = findById(id);
        if (oferente == null) return "Oferente no encontrado";
        oferente.setAutorizado(true);
        oferenteRepository.save(oferente);
        Usuario usuario = oferente.getUsuario();
        usuario.setActivo(true);
        usuarioService.guardarSinHash(usuario);
        return null;
    }

    public void actualizarCurriculum(Integer id, String ruta) {
        Oferente oferente = findById(id);
        if (oferente != null) {
            oferente.setCurriculum(ruta);
            oferenteRepository.save(oferente);
        }
    }
}
