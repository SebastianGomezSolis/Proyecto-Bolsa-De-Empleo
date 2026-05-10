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

// Servicio para la carga automatica de nacionalidades desde un archivo Excel.
// Al iniciar la aplicacion, verifica si ya hay nacionalidades cargadas.
// Si no las hay, carga los datos desde nacionalidades.xlsx (classpath).
@Component
public class NacionalidadExcelLoader implements CommandLineRunner {
    // Servicio para acceder a nacionalidades en la base de datos
    @Autowired
    private NacionalidadService nacionalidadService;

    // Metodo que se ejecuta automaticamente al iniciar la aplicacion.
    // Carga las nacionalidades desde el archivo Excel solo si la BD esta vacia.
    @Override
    public void run(String... args) throws Exception {
        // Si ya hay nacionalidades en la BD, no cargar nuevamente
        if (nacionalidadService.count() > 0) {
            return;
        }

        // Cargar archivo Excel desde el classpath de la aplicacion
        ClassPathResource resource = new ClassPathResource("nacionalidades.xlsx");

        // Abrir el archivo Excel y procesar cada fila
        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            // Obtener la primera hoja del workbook
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(); // Formatea celdas a texto

            // Iterar sobre todas las filas de la hoja
            for (Row row : sheet) {
                // Saltar la primera fila (encabezados)
                if (row.getRowNum() == 0) continue;

                // Extraer valores de cada columna
                String iso = formatter.formatCellValue(row.getCell(0)).trim();
                String nombre = formatter.formatCellValue(row.getCell(1)).trim();
                String descripcion = formatter.formatCellValue(row.getCell(2)).trim();
                String iso3 = formatter.formatCellValue(row.getCell(3)).trim();
                String codigoNumeroTexto = formatter.formatCellValue(row.getCell(4)).trim();
                String codigoTelefonoTexto = formatter.formatCellValue(row.getCell(5)).trim();

                // Ignorar filas vacias o sin datos esenciales
                if (iso.isBlank() || nombre.isBlank()) continue;

                // Crear y populate el objeto Nacionalidad
                Nacionalidad nacionalidad = new Nacionalidad();
                nacionalidad.setIso(iso);
                nacionalidad.setNombre(nombre);
                nacionalidad.setDescripcion(descripcion.isBlank() ? null : descripcion);
                nacionalidad.setIso3(iso3.isBlank() ? null : iso3);
                nacionalidad.setCodigoNumero(parseEntero(codigoNumeroTexto));
                nacionalidad.setCodigoTelefono(parseEntero(codigoTelefonoTexto));

                // Guardar en la base de datos
                nacionalidadService.save(nacionalidad);
            }
        }
    }

    // Convierte una cadena de texto a entero.
    // Limpia ".0" de numeros decimales y maneja valores invalidos.
    private Integer parseEntero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            // Remover ".0" de numeros como "506.0" -> "506"
            return Integer.parseInt(valor.replace(".0", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
