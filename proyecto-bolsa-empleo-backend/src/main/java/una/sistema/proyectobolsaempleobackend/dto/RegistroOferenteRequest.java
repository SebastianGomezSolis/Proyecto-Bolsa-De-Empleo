package una.sistema.proyectobolsaempleobackend.dto;

public class RegistroOferenteRequest {
    private String correo;
    private String clave;
    private String identificacion;
    private String nombre;
    private String primerApellido;
    private String isoNacionalidad;
    private String telefono;
    private String lugarResidencia;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public String getIsoNacionalidad() { return isoNacionalidad; }
    public void setIsoNacionalidad(String isoNacionalidad) { this.isoNacionalidad = isoNacionalidad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getLugarResidencia() { return lugarResidencia; }
    public void setLugarResidencia(String lugarResidencia) { this.lugarResidencia = lugarResidencia; }
}
