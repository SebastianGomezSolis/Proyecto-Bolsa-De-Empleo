package una.sistema.proyectobolsaempleobackend.logic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Entidad que representa a una empresa registrada en el sistema.
// Las empresas pueden crear y administrar ofertas de empleo (puestos).
@Entity
@Table(name = "empresa")
@Getter
@Setter
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String localizacion;

    @Column(length = 20)
    private String telefono;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Boolean autorizado = false;

    @JsonProperty("usuarioCorreo")
    public String getUsuarioCorreo() {
        return usuario != null ? usuario.getCorreo() : null;
    }
}
