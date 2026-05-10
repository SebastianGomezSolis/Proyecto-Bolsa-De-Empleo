package una.sistema.proyectobolsaempleobackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.SesionUsuarioBean;

import java.io.IOException;

// Filtro JWT que intercepta todas las peticiones HTTP.
// Verifica la presencia de un token JWT en el header Authorization o en los parametros de consulta, y si es valido, carga la sesion del usuario.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // Servicio para generar y validar tokens JWT
    @Autowired
    private JwtService jwtService;

    // Bean de sesion para almacenar los datos del usuario autenticado
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    // Metodo que se ejecuta en cada peticion HTTP.
    // Es el punto de entrada del filtro para procesar el token JWT.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Limpiar la sesion antes de procesar (evita datos de sesiones anteriores)
        sesionUsuarioBean.logout();

        // Extraer el token de diferentes fuentes posibles
        String authHeader = request.getHeader("Authorization");
        String token = null;

        // Intentar obtener token del header Authorization (formato: "Bearer <token>")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remover prefijo "Bearer "
        } else if (request.getParameter("token") != null) {
            // Si no hay header, buscar en parametros de consulta (?token=xxx)
            token = request.getParameter("token");
        }

        // Si se encontro un token, intentar validarlo y cargar la sesion
        if (token != null && jwtService.esValido(token)) {
            // Extraer informacion del token JWT
            Integer id = jwtService.obtenerUserId(token);
            String correo = jwtService.obtenerCorreo(token);
            Rol rol = jwtService.obtenerRol(token);
            Integer referenciaId = jwtService.obtenerReferenciaId(token);

            // Cargar datos del usuario en el bean de sesion
            sesionUsuarioBean.login(id, correo, rol, referenciaId);
        }

        // Continuar con el resto de la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
