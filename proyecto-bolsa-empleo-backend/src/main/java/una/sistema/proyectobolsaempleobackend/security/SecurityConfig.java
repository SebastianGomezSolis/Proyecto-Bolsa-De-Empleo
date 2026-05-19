package una.sistema.proyectobolsaempleobackend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Configuracion de seguridad Spring Security.
// Define politicas CORS, deshabilita CSRF, establece sesiones stateless,
// protege endpoints segun el rol y agrega el filtro JWT.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Filtro que intercepta peticiones para validar el token JWT
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Permitir peticiones desde el frontend (CORS)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitar CSRF (no necesario con JWT stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // No crear sesiones HTTP en el servidor
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Definir reglas de autorizacion por endpoint
                .authorizeHttpRequests(auth ->
                        auth
                                // Endpoints publicos (login, registro, sesion)
                                .requestMatchers("/api/auth/**").permitAll()
                                // Datos publicos (puestos, nacionalidades, caracteristicas)
                                .requestMatchers("/api/publico/**").permitAll()
                                // Busqueda publica de puestos
                                .requestMatchers("/api/puestos/**").permitAll()
                                // Solo empresas autenticadas
                                .requestMatchers("/api/empresa/**").authenticated()
                                // Solo administradores autenticados
                                .requestMatchers("/api/admin/**").authenticated()
                                // Solo oferentes autenticados
                                .requestMatchers("/api/oferente/**").authenticated()
                                // Cualquier otra ruta sin restriccion
                                .anyRequest().permitAll()
                )
                // Respuesta JSON personalizada para 401 (no autenticado)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"No autenticado\"}");
                        })
                )
                // Ejecutar el filtro JWT antes del filtro de autenticacion de usuario/contra
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configura los origenes y metodos permitidos para CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Origenes permitidos (frontend en desarrollo)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));
        // Metodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));
        // No enviar cookies en peticiones cross-origin
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
