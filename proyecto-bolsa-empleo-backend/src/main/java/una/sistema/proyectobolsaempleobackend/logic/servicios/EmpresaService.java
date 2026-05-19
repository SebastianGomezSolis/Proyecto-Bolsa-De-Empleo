package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.data.EmpresaRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Empresa;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;

import java.util.List;

// Servicio para la gestion de empresas.
// Maneja el registro de nuevas empresas, consulta de empresas y la autorizacion de empresas pendientes por parte de un administrador.
@Service
public class EmpresaService {
    // Repositorio para operaciones CRUD con la entidad Empresa
    @Autowired
    private EmpresaRepository empresaRepository;

    // Servicio para gestionar usuarios (validaciones de correo, etc.)
    @Autowired
    private UsuarioService usuarioService;

    // Retorna todas las empresas registradas en el sistema
    public Iterable<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    // Busca una empresa por su ID. Retorna null si no existe.
    public Empresa findById(Integer id) {
        return empresaRepository.findById(id).orElse(null);
    }

    // Busca una empresa por el correo de su usuario. Retorna null si no existe.
    public Empresa findByCorreo(String correo) {
        return empresaRepository.findByUsuario_Correo(correo).orElse(null);
    }

    // Busca una empresa por su nombre. Retorna null si no existe.
    public Empresa findByNombre(String nombre) {
        return empresaRepository.findByNombre(nombre).orElse(null);
    }

    // Retorna todas las empresas que aun no han sido autorizadas
    // (pendientes de aprobacion por un administrador)
    public List<Empresa> findPendientes() {
        return empresaRepository.findByAutorizadoFalse();
    }

    // Guarda una empresa directamente en la base de datos.
    public Empresa save(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    // Registra una nueva empresa en el sistema.
    // Valida campos requeridos, verifica unicidad de correo,
    // asigna rol EMPRESA y guarda al usuario asociado.
    // Retorna null si el registro es exitoso, o un mensaje de error en caso contrario.
    public String registrar(Empresa empresa) {
        // Validar que el usuario asociado no sea null
        if (empresa.getUsuario() == null)
            return "El usuario es requerido";

        // Validar que el correo no sea vacio
        if (empresa.getUsuario().getCorreo() == null || empresa.getUsuario().getCorreo().isBlank())
            return "El correo es requerido";

        // Validar que la contrasena no sea vacia
        if (empresa.getUsuario().getClave() == null || empresa.getUsuario().getClave().isBlank())
            return "La clave es requerida";

        // Validar que el nombre de la empresa no sea vacio
        if (empresa.getNombre() == null || empresa.getNombre().isBlank())
            return "El nombre es requerido";

        // Verificar que el correo no este ya registrado en el sistema
        if (usuarioService.existeCorreo(empresa.getUsuario().getCorreo()))
            return "El correo ya está registrado";

        // Verificar que el nombre de la empresa no este duplicado
        if (empresaRepository.existsByNombre(empresa.getNombre()))
            return "El nombre de la empresa ya está registrado";

        // Asignar rol de empresa al usuario
        empresa.getUsuario().setRol(Rol.EMPRESA);

        // Desactivar usuario hasta que un admin lo autorice
        empresa.getUsuario().setActivo(false);

        // Guardar usuario con contrasena hasheada
        Usuario guardado = usuarioService.guardar(empresa.getUsuario());
        empresa.setUsuario(guardado);

        // Guardar empresa como no autorizada inicialmente
        empresa.setAutorizado(false);
        empresaRepository.save(empresa);
        return null;
    }

    // Autoriza a una empresa previamente registrada.
    // Activa al usuario de la empresa para que pueda iniciar sesion.
    // Retorna null si la autorizacion es exitosa, o un mensaje de error.
    public String autorizar(Integer id) {
        // Buscar empresa por ID
        Empresa empresa = findById(id);
        if (empresa == null) return "Empresa no encontrada";

        // Marcar empresa como autorizada
        empresa.setAutorizado(true);
        empresaRepository.save(empresa);

        // Activar el usuario asociado para permitir login
        Usuario usuario = empresa.getUsuario();
        usuario.setActivo(true);
        usuarioService.guardarSinHash(usuario);
        return null;
    }
}
