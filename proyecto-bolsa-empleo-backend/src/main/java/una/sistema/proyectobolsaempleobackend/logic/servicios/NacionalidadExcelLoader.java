package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import una.sistema.proyectobolsaempleobackend.logic.model.Nacionalidad;

import java.io.InputStream;

@Component
public class NacionalidadExcelLoader implements CommandLineRunner {
    @Autowired
    private NacionalidadService nacionalidadService;

    @Override
    public void run(String... args) throws Exception {
        if (nacionalidadService.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("nacionalidades.xlsx");

        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String iso = formatter.formatCellValue(row.getCell(0)).trim();
                String nombre = formatter.formatCellValue(row.getCell(1)).trim();
                String descripcion = formatter.formatCellValue(row.getCell(2)).trim();
                String iso3 = formatter.formatCellValue(row.getCell(3)).trim();
                String codigoNumeroTexto = formatter.formatCellValue(row.getCell(4)).trim();
                String codigoTelefonoTexto = formatter.formatCellValue(row.getCell(5)).trim();

                if (iso.isBlank() || nombre.isBlank()) continue;

                Nacionalidad nacionalidad = new Nacionalidad();
                nacionalidad.setIso(iso);
                nacionalidad.setNombre(nombre);
                nacionalidad.setDescripcion(descripcion.isBlank() ? null : descripcion);
                nacionalidad.setIso3(iso3.isBlank() ? null : iso3);
                nacionalidad.setCodigoNumero(parseEntero(codigoNumeroTexto));
                nacionalidad.setCodigoTelefono(parseEntero(codigoTelefonoTexto));

                nacionalidadService.save(nacionalidad);
            }
        }
    }

    private Integer parseEntero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.replace(".0", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
