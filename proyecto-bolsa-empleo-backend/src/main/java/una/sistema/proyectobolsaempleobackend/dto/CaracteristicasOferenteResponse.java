package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

public class CaracteristicasOferenteResponse {
    private List<Caracteristica> subcategorias;
    private Caracteristica actual;

    public CaracteristicasOferenteResponse() {}

    public CaracteristicasOferenteResponse(List<Caracteristica> subcategorias, Caracteristica actual) {
        this.subcategorias = subcategorias;
        this.actual = actual;
    }

    public List<Caracteristica> getSubcategorias() { return subcategorias; }
    public void setSubcategorias(List<Caracteristica> subcategorias) { this.subcategorias = subcategorias; }
    public Caracteristica getActual() { return actual; }
    public void setActual(Caracteristica actual) { this.actual = actual; }
}
