package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.util.List;

public class BuscarPuestosResponse {
    private List<Puesto> puestos;
    private List<Caracteristica> raices;
    private Object tipoCambio;
    private List<Integer> caracteristicaIds;

    public BuscarPuestosResponse() {}

    public BuscarPuestosResponse(List<Puesto> puestos, List<Caracteristica> raices, Object tipoCambio, List<Integer> caracteristicaIds) {
        this.puestos = puestos;
        this.raices = raices;
        this.tipoCambio = tipoCambio;
        this.caracteristicaIds = caracteristicaIds;
    }

    public List<Puesto> getPuestos() { return puestos; }
    public void setPuestos(List<Puesto> puestos) { this.puestos = puestos; }
    public List<Caracteristica> getRaices() { return raices; }
    public void setRaices(List<Caracteristica> raices) { this.raices = raices; }
    public Object getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(Object tipoCambio) { this.tipoCambio = tipoCambio; }
    public List<Integer> getCaracteristicaIds() { return caracteristicaIds; }
    public void setCaracteristicaIds(List<Integer> caracteristicaIds) { this.caracteristicaIds = caracteristicaIds; }
}
