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

// Servicio para la gestion de oferentes (candidatos求职者として).
// Maneja el registro de oferentes, consulta de datos,
// autorizacion por parte de un administrador y actualizacion de curriculum.
@Service
public class OferenteService {
    // Repositorio para operaciones CRUD con la entidad Oferente
    @Autowired
    private OferenteRepository oferenteRepository;

    // Servicio para gestionar usuarios
    @Autowired
    private UsuarioService usuarioService;

    // Repositorio para buscar nacionalidades por codigo ISO
    @Autowired
    private NacionalidadRepository nacionalidadRepository;

    // Retorna todos los oferentes registrados
    public Iterable<Oferente> findAll() {
        return oferenteRepository.findAll();
    }

    // Busca un oferente por su ID. Retorna null si no existe.
    public Oferente findById(Integer id) {
        return oferenteRepository.findById(id).orElse(null);
    }

    // Busca un oferente por el correo de su usuario. Retorna null si no existe.
    public Oferente findByCorreo(String correo) {
        return oferenteRepository.findByUsuario_Correo(correo).orElse(null);
    }

    // Busca un oferente por su identificacion (cedula). Retorna null si no existe.
    public Oferente findByIdentificacion(String identificacion) {
        return oferenteRepository.findByIdentificacion(identificacion).orElse(null);
    }

    // Retorna todos los oferentes pendientes de autorizacion
    public List<Oferente> findPendientes() {
        return oferenteRepository.findByAutorizadoFalse();
    }

    // Guarda un oferente directamente en la base de datos.
    public Oferente save(Oferente oferente) {
        return oferenteRepository.save(oferente);
    }

    // Registra un nuevo oferente en el sistema.
    // Valida todos los campos requeridos, verifica unicidad de correo e identificacion,
    // resuelve la nacionalidad y asigna el rol OFERENTE.
    // Retorna null si es exitoso, o un mensaje de error.
    public String registrar(Oferente oferente) {
        // Validar que el usuario no sea null
        if (oferente.getUsuario() == null)
            return "El usuario es requerido";

        // Validar correo
        if (oferente.getUsuario().getCorreo() == null || oferente.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        // Validar contrasena
        if (oferente.getUsuario().getClave() == null || oferente.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        // Validar identificacion
        if (oferente.getIdentificacion() == null || oferente.getIdentificacion().isBlank())
            return "La identificación es requerida";

        // Validar nombre
        if (oferente.getNombre() == null || oferente.getNombre().isBlank())
            return "El nombre es requerido";

        // Validar primer apellido
        if (oferente.getPrimerApellido() == null || oferente.getPrimerApellido().isBlank())
            return "El primer apellido es requerido";

        // Verificar que el correo no este registrado
        if (usuarioService.existeCorreo(oferente.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        // Verificar que la identificacion no este duplicada
        if (oferenteRepository.existsByIdentificacion(oferente.getIdentificacion()))
            return "La identificación ya está registrada";

        // Validar que la nacionalidad no sea null y tenga codigo ISO
        if (oferente.getNacionalidad() == null || oferente.getNacionalidad().getIso() == null)
            return "Nacionalidad inválida";

        // Buscar la nacionalidad en la BD para verificar que existe
        Nacionalidad nac = nacionalidadRepository.findById(oferente.getNacionalidad().getIso()).orElse(null);
        if (nac == null)
            return "Nacionalidad inválida";

        // Asignar rol OFERENTE al usuario
        oferente.getUsuario().setRol(Rol.OFERENTE);

        // Desactivar usuario hasta autorizacion del admin
        oferente.getUsuario().setActivo(false);

        // Guardar usuario con contrasena hasheada
        Usuario guardado = usuarioService.guardar(oferente.getUsuario());
        oferente.setUsuario(guardado);
        oferente.setNacionalidad(nac);

        // Guardar oferente como no autorizado inicialmente
        oferente.setAutorizado(false);
        oferenteRepository.save(oferente);
        return null;
    }

    // Autoriza a un oferente previamente registrado.
    // Activa su usuario para permitir el inicio de sesion.
    public String autorizar(Integer id) {
        Oferente oferente = findById(id);
        if (oferente == null) return "Oferente no encontrado";

        // Marcar oferente como autorizado
        oferente.setAutorizado(true);
        oferenteRepository.save(oferente);

        // Activar el usuario asociado
        Usuario usuario = oferente.getUsuario();
        usuario.setActivo(true);
        usuarioService.guardarSinHash(usuario);
        return null;
    }

    // Actualiza la ruta del archivo de curriculum (CV) del oferente.
    // Se llama tras subir exitosamente el archivo PDF.
    public void actualizarCurriculum(Integer id, String ruta) {
        Oferente oferente = findById(id);
        if (oferente != null) {
            oferente.setCurriculum(ruta);
            oferenteRepository.save(oferente);
        }
    }
}
