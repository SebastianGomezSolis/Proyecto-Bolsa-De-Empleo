package una.sistema.proyectobolsaempleobackend.dto;

public class SesionResponse {
    private Integer id;
    private String correo;
    private String rol;
    private Integer referenciaId;

    public SesionResponse() {}

    public SesionResponse(Integer id, String correo, String rol, Integer referenciaId) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.referenciaId = referenciaId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }
}
