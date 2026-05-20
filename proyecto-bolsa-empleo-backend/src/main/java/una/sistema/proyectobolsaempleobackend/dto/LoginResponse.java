package una.sistema.proyectobolsaempleobackend.dto;

public class LoginResponse {
    private Integer id;
    private String correo;
    private String rol;
    private Integer referenciaId;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Integer id, String correo, String rol, Integer referenciaId, String token) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.referenciaId = referenciaId;
        this.token = token;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
