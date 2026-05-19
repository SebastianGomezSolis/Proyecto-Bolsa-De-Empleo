package una.sistema.proyectobolsaempleobackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;
import una.sistema.proyectobolsaempleobackend.logic.model.SesionUsuarioBean;

import java.io.IOException;
import java.util.Collections;

// Filtro que intercepta todas las peticiones HTTP para validar el token JWT.
// Si el token es valido, carga los datos del usuario en la sesion y en el contexto de seguridad.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Servicio para generar y validar tokens JWT
    @Autowired
    private JwtService jwtService;

    // Bean de sesion con los datos del usuario autenticado
    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Limpiar sesion previa antes de procesar la peticion
        sesionUsuarioBean.logout();

        // Extraer el token del header Authorization o de los parametros de consulta
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Formato esperado: "Bearer <token>"
            token = authHeader.substring(7);
        } else if (request.getParameter("token") != null) {
            // Alternativa: token como query param (ej: ?token=xxx)
            token = request.getParameter("token");
        }

        // Si se encontro un token y es valido, cargar la sesion del usuario
        if (token != null && jwtService.esValido(token)) {
            // Extraer datos del usuario desde el token JWT
            Integer id = jwtService.obtenerUserId(token);
            String correo = jwtService.obtenerCorreo(token);
            Rol rol = jwtService.obtenerRol(token);
            Integer referenciaId = jwtService.obtenerReferenciaId(token);

            // Cargar datos en el bean de sesion (para chequeos de rol en controllers)
            sesionUsuarioBean.login(id, correo, rol, referenciaId);

            // Establecer autenticacion en el contexto de Spring Security
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(correo, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
