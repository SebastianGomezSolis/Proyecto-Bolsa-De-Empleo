package una.sistema.proyectobolsaempleobackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import una.sistema.proyectobolsaempleobackend.data.UsuarioRepository;
import una.sistema.proyectobolsaempleobackend.logic.model.Administrador;
import una.sistema.proyectobolsaempleobackend.logic.model.Usuario;
import una.sistema.proyectobolsaempleobackend.logic.servicios.AdministradorService;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorService administradorService;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByCorreo("admin@sistema.com")) {
            Usuario usuario = new Usuario();
            usuario.setCorreo("admin@sistema.com");
            usuario.setClave("admin123");

            Administrador admin = new Administrador();
            admin.setUsuario(usuario);
            admin.setIdentificacion("ADM-001");
            admin.setNombre("Administrador");

            administradorService.crear(admin);
            System.out.println("Admin creado: admin@sistema.com / admin123");
        }
    }
}
