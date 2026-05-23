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

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Integer id, String correo, Rol rol, Integer referenciaId) {
        return Jwts.builder()
                .subject(correo)
                .claim("id", id)
                .claim("rol", rol.name())
                .claim("referenciaId", referenciaId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(clave())
                .compact();
    }

    public Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerUsername(String token) {
        return parsearClaims(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String obtenerCorreo(String token) {
        return parsearClaims(token).getSubject();
    }

    public Integer obtenerUserId(String token) {
        return parsearClaims(token).get("id", Integer.class);
    }

    public Rol obtenerRol(String token) {
        return Rol.valueOf(parsearClaims(token).get("rol", String.class));
    }

    public Integer obtenerReferenciaId(String token) {
        return parsearClaims(token).get("referenciaId", Integer.class);
    }
}
