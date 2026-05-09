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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private SesionUsuarioBean sesionUsuarioBean;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        sesionUsuarioBean.logout();

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request.getParameter("token") != null) {
            token = request.getParameter("token");
        }

        if (token != null && jwtService.esValido(token)) {
            Integer id = jwtService.obtenerUserId(token);
            String correo = jwtService.obtenerCorreo(token);
            Rol rol = jwtService.obtenerRol(token);
            Integer referenciaId = jwtService.obtenerReferenciaId(token);

            sesionUsuarioBean.login(id, correo, rol, referenciaId);
        }

        filterChain.doFilter(request, response);
    }
}
