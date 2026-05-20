package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Habilidad;
import una.sistema.proyectobolsaempleobackend.logic.model.Oferente;

import java.util.List;

public class DetalleCandidatoResponse {
    private Oferente oferente;
    private List<Habilidad> habilidades;
    private Object puesto;

    public DetalleCandidatoResponse() {}

    public DetalleCandidatoResponse(Oferente oferente, List<Habilidad> habilidades, Object puesto) {
        this.oferente = oferente;
        this.habilidades = habilidades;
        this.puesto = puesto;
    }

    public Oferente getOferente() { return oferente; }
    public void setOferente(Oferente oferente) { this.oferente = oferente; }
    public List<Habilidad> getHabilidades() { return habilidades; }
    public void setHabilidades(List<Habilidad> habilidades) { this.habilidades = habilidades; }
    public Object getPuesto() { return puesto; }
    public void setPuesto(Object puesto) { this.puesto = puesto; }
}
