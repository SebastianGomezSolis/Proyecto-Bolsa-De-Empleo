package una.sistema.proyectobolsaempleobackend.logic.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SesionUsuarioBean {
    private Integer id;
    private String correo;
    private Rol rol;
    private Integer referenciaId;

    public void login(Integer id, String correo, Rol rol, Integer referenciaId) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.referenciaId = referenciaId;
    }

    public void logout() {
        id = null;
        correo = null;
        rol = null;
        referenciaId = null;
    }

    public boolean isLogueado() { return id != null; }
    public boolean isAdmin() { return isLogueado() && rol == Rol.ADMIN; }
    public boolean isEmpresa() { return isLogueado() && rol == Rol.EMPRESA; }
    public boolean isOferente() { return isLogueado() && rol == Rol.OFERENTE; }

    public Integer getId() { return id; }
    public String getCorreo() { return correo; }
    public Rol getRol() { return rol; }
    public Integer getReferenciaId() { return referenciaId; }
}
