package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

public class DetallePuestoResponse {
    private Puesto puesto;
    private Object tipoCambio;

    public DetallePuestoResponse(Puesto puesto, Object tipoCambio) {
        this.puesto = puesto;
        this.tipoCambio = tipoCambio;
    }

    public Puesto getPuesto() { return puesto; }
    public void setPuesto(Puesto puesto) { this.puesto = puesto; }
    public Object getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(Object tipoCambio) { this.tipoCambio = tipoCambio; }
}
