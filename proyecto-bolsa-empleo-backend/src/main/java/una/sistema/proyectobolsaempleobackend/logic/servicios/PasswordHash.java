package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHash {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String plain) {
        return encoder.encode(plain);
    }

    public boolean verify(String plain, String hashed) {
        return encoder.matches(plain, hashed);
    }
}
