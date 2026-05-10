package una.sistema.proyectobolsaempleobackend.logic.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

// Bean de sesion que almacena la informacion del usuario logueado.
// Se mantiene durante toda la sesion HTTP y permite acceder a los datos
// del usuario autenticado desde cualquier parte de la aplicacion.
@Component
@SessionScope
public class SesionUsuarioBean {
    // ID unico del usuario en la sesion
    private Integer id;

    // Correo electronico del usuario
    private String correo;

    // Rol del usuario (ADMIN, EMPRESA u OFERENTE)
    private Rol rol;

    // ID de referencia que vincula al usuario con su entidad especifica
    // (Administrador.id, Empresa.id u Oferente.id)
    private Integer referenciaId;

    // Metodo para inicializar la sesion con los datos del usuario que inicia sesion.
    // Recibe el ID del usuario, correo, rol y el ID de referencia correspondiente.
    public void login(Integer id, String correo, Rol rol, Integer referenciaId) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.referenciaId = referenciaId;
    }

    // Metodo para cerrar la sesion, limpiando todos los datos del usuario.
    public void logout() {
        id = null;
        correo = null;
        rol = null;
        referenciaId = null;
    }

    // Retorna true si hay un usuario logueado en la sesion
    public boolean isLogueado() { return id != null; }

    // Retorna true si el usuario logueado tiene rol de ADMIN
    public boolean isAdmin() { return isLogueado() && rol == Rol.ADMIN; }

    // Retorna true si el usuario logueado tiene rol de EMPRESA
    public boolean isEmpresa() { return isLogueado() && rol == Rol.EMPRESA; }

    // Retorna true si el usuario logueado tiene rol de OFERENTE
    public boolean isOferente() { return isLogueado() && rol == Rol.OFERENTE; }

    public Integer getId() { return id; }
    public String getCorreo() { return correo; }
    public Rol getRol() { return rol; }
    public Integer getReferenciaId() { return referenciaId; }
}
