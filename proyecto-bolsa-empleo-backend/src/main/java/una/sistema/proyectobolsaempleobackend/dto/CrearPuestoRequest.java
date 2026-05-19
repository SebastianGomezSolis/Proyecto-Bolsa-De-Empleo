package una.sistema.proyectobolsaempleobackend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CrearPuestoRequest {
    private String descripcion;
    private BigDecimal salario;
    private String tipoPublicacion;
    private List<Integer> caracteristicaIds;
    private Map<String, String> niveles;

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public String getTipoPublicacion() { return tipoPublicacion; }
    public void setTipoPublicacion(String tipoPublicacion) { this.tipoPublicacion = tipoPublicacion; }
    public List<Integer> getCaracteristicaIds() { return caracteristicaIds; }
    public void setCaracteristicaIds(List<Integer> caracteristicaIds) { this.caracteristicaIds = caracteristicaIds; }
    public Map<String, String> getNiveles() { return niveles; }
    public void setNiveles(Map<String, String> niveles) { this.niveles = niveles; }
}
