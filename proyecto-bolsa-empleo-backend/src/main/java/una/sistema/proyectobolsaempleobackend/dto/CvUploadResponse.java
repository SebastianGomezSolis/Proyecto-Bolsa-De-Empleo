package una.sistema.proyectobolsaempleobackend.dto;

public class CvUploadResponse {
    private String ruta;

    public CvUploadResponse(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
}
