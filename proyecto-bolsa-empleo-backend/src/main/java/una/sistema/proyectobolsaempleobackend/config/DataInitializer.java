package una.sistema.proyectobolsaempleobackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import una.sistema.proyectobolsaempleobackend.data.UsuarioRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;
import una.sistema.proyectobolsaempleobackend.logic.servicios.AdministradorService;

// Inicializador de datos que se ejecuta al iniciar la aplicacion.
// Verifica si ya existe un administrador inicial en la base de datos,
// y si no existe, lo crea automaticamente.
// Esto asegura que siempre haya un administrador para gestionar el sistema.
@Component
public class DataInitializer implements CommandLineRunner {
    // Repositorio para verificar si el correo del administrador ya existe
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Servicio para crear administradores
    @Autowired
    private AdministradorService administradorService;

    // Metodo que se ejecuta automaticamente al iniciar el aplicativo.
    // Crea el administrador inicial si no existe.
    @Override
    public void run(String... args) {
        // Verificar si ya existe un usuario con el correo del administrador inicial
        if (!usuarioRepository.existsByCorreo("admin@sistema.com")) {
            // Crear el usuario del administrador
            Usuario usuario = new Usuario();
            usuario.setCorreo("admin@sistema.com");
            usuario.setClave("admin123");

            // Crear el objeto Administrador y asociar el usuario
            Administrador admin = new Administrador();
            admin.setUsuario(usuario);
            admin.setIdentificacion("ADM-001");
            admin.setNombre("Administrador");

            // Guardar el administrador en la base de datos
            administradorService.crear(admin);

            // Imprimir mensaje en consola con las credenciales del admin inicial
            System.out.println("Admin creado: admin@sistema.com / admin123");
        }
    }
}
