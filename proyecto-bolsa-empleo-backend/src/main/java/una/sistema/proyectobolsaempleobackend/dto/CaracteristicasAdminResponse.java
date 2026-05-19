package una.sistema.proyectobolsaempleobackend.dto;

import una.sistema.proyectobolsaempleobackend.logic.model.Caracteristica;

import java.util.List;

public class CaracteristicasAdminResponse {
    private List<Caracteristica> subcategorias;
    private Caracteristica actual;
    private List<Caracteristica> ruta;
    private List<Caracteristica> todas;

    public List<Caracteristica> getSubcategorias() { return subcategorias; }
    public void setSubcategorias(List<Caracteristica> subcategorias) { this.subcategorias = subcategorias; }
    public Caracteristica getActual() { return actual; }
    public void setActual(Caracteristica actual) { this.actual = actual; }
    public List<Caracteristica> getRuta() { return ruta; }
    public void setRuta(List<Caracteristica> ruta) { this.ruta = ruta; }
    public List<Caracteristica> getTodas() { return todas; }
    public void setTodas(List<Caracteristica> todas) { this.todas = todas; }
}
