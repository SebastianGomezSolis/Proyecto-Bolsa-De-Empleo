package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.List;

public class PuestosPublicosResponse {
    private List<Puesto> puestos;
    private Object tipoCambio;

    public List<Puesto> getPuestos() { return puestos; }
    public void setPuestos(List<Puesto> puestos) { this.puestos = puestos; }
    public Object getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(Object tipoCambio) { this.tipoCambio = tipoCambio; }
}
