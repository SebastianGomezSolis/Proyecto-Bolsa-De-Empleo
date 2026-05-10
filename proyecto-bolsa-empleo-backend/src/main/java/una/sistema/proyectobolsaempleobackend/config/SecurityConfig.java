package una.sistema.proyectobolsaempleobackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import una.sistema.proyectobolsaempleobackend.security.JwtAuthFilter;

import java.util.List;

// Configuracion de seguridad de Spring Security.
// Configura politicas CORS, deshabilita CSRF, establece autenticacion stateless,
// y agrega el filtro JWT en la cadena de filtros de seguridad.
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Filtro JWT para inyectar en la cadena de filtros
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // Configura la cadena de filtros de seguridad de Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuracion CORS personalizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitar proteccion CSRF (no necesaria con JWT stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Configurar gestion de sesiones como stateless (sin estado en servidor)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configurar autorizacion de peticiones: permitir todo (la logica de autorizacion esta en los controllers)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll())
                // Agregar el filtro JWT antes del filtro de autenticacion de usuario/contrasena
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configura los origenes y metodos permitidos para CORS (Cross-Origin Resource Sharing).
    // Permite que el frontend (en otro puerto) pueda hacer peticiones al backend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Origenes permitidos: frontend en desarrollo (localhost:3000 y 5173)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        // Metodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Headers permitidos en las peticiones
        config.setAllowedHeaders(List.of("*"));
        // No permitir envio de credenciales (cookies) en peticiones cross-origin
        config.setAllowCredentials(false);

        // Registrar la configuracion CORS para todas las rutas que empiecen con /api/
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
