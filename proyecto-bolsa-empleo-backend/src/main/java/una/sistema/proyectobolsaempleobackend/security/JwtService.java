package una.sistema.proyectobolsaempleobackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.logic.model.Rol;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Servicio para la generacion y validacion de tokens JWT (JSON Web Tokens).
// Los tokens JWT se usan para autenticar solicitudes HTTP de forma stateless.
// Contienen informacion del usuario (ID, correo, rol) y tienen fecha de expiracion.
@Service
public class JwtService {
    // Clave secreta para firmar los tokens (configurada en application.properties)
    @Value("${app.jwt.secret}")
    private String secret;

    // Tiempo de expiracion del token en milisegundos (configurado en application.properties)
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Construye la clave HMAC-SHA a partir de la cadena secreta
    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Genera un nuevo token JWT con la informacion del usuario.
    // Incluye: ID, correo, rol, referenciaId, fecha de emision y fecha de expiracion.
    public String generarToken(Integer id, String correo, Rol rol, Integer referenciaId) {
        return Jwts.builder()
                .subject(correo)  // El "subject" es el correo del usuario
                .claim("id", id)  // Claim personalizado: ID del usuario
                .claim("rol", rol.name())  // Claim personalizado: nombre del rol
                .claim("referenciaId", referenciaId)  // Claim: ID de referencia
                .issuedAt(new Date())  // Fecha de emision del token
                .expiration(new Date(System.currentTimeMillis() + expirationMs))  // Fecha de expiracion
                .signWith(clave())  // Firmar con la clave secreta HMAC
                .compact();  // Generar representacion en string del token
    }

    // Parsea y retorna los "claims" (datos) contenidos en un token JWT.
    // Verifica automaticamente la firma del token.
    // Lanza excepcion si el token es invalido o esta alterado.
    public Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave())  // Verificar firma con la clave secreta
                .build()
                .parseSignedClaims(token)  // Parsear claims del token firmado
                .getPayload();  // Obtener los datos (payload) decodificados
    }

    // Verifica si un token JWT es valido.
    // Retorna true si: la firma es correcta, no esta expirado, y los datos son legibles.
    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception e) {
            // Si cualquier excepcion ocurre (firma invalida, token expirado, etc.), retorna false
            return false;
        }
    }

    // Extrae el correo (subject) del token JWT
    public String obtenerCorreo(String token) {
        return parsearClaims(token).getSubject();
    }

    // Extrae el ID del usuario del token JWT
    public Integer obtenerUserId(String token) {
        return parsearClaims(token).get("id", Integer.class);
    }

    // Extrae el rol del usuario del token JWT y lo convierte a enum
    public Rol obtenerRol(String token) {
        return Rol.valueOf(parsearClaims(token).get("rol", String.class));
    }

    // Extrae el ID de referencia del token JWT
    public Integer obtenerReferenciaId(String token) {
        return parsearClaims(token).get("referenciaId", Integer.class);
    }
}
