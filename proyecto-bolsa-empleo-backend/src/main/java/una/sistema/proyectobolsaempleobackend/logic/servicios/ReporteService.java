package una.sistema.proyectobolsaempleobackend.logic.servicios;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Servicio para generar reportes en formato PDF.
// Crea un documento PDF con informacion de puestos publicados
// en un periodo especifico (mes y ano).
@Service
public class ReporteService {
    // Servicio para acceder a los puestos de trabajo
    @Autowired
    private PuestoService puestoService;

    // Genera un reporte PDF con los puestos publicados en un mes y ano especifico.
    // El PDF incluye un encabezado, lista de puestos con sus datos,
    // y paginacion automatica si hay muchos resultados.
    public byte[] generarPdfPuestosPorMesYAnio(int mes, int anio) {
        // Crear rango de fechas para el periodo especificado
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // Obtener puestos del periodo
        List<Puesto> puestos = puestoService.findPorFechaRegistroEntre(inicio, fin);
        String nombreMes = nombreMes(mes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Crear fuentes para el PDF (negrita, regular, cursiva)
        PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font italic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Crear primera pagina del documento
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            // Obtener dimensiones de la pagina
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth  = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();
            float margin = 48;

            // Crear flujo de contenido para dibujar en la pagina
            PDPageContentStream content = new PDPageContentStream(document, page);

            // Dibujar encabezado con titulo y periodo
            drawHeader(content, pageWidth, pageHeight, margin, bold, regular, nombreMes, anio);

            // Posicionar cursor Y debajo del encabezado
            float y = pageHeight - 125;

            // Escribir resumen general
            y = writeLine(content, "Resumen general", margin, y, bold, 13);
            y = writeLine(content, "Total de puestos encontrados: " + puestos.size(), margin, y, regular, 11);
            y -= 12;

            // Si no hay puestos en el periodo, mostrar mensaje
            if (puestos.isEmpty()) {
                writeLine(content, "No se encontraron puestos en el período seleccionado.", margin, y, regular, 11);
            } else {
                int contador = 1;

                // Iterar sobre cada puesto y escribir sus datos
                for (Puesto puesto : puestos) {
                    // Si no hay espacio suficiente, crear nueva pagina
                    if (y < 150) {
                        drawFooter(content, margin, pageWidth, italic);
                        content.close();

                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        mediaBox = page.getMediaBox();
                        pageWidth  = mediaBox.getWidth();
                        pageHeight = mediaBox.getHeight();
                        y = pageHeight - 50;

                        content = new PDPageContentStream(document, page);
                        drawHeader(content, pageWidth, pageHeight, margin, bold, regular, nombreMes, anio);
                        y = pageHeight - 125;
                    }

                    // Escribir titulo de seccion (numero de puesto)
                    y = drawSectionTitle(content, margin, y, bold, "Puesto " + contador);

                    // Escribir campos del puesto
                    y = drawField(content, margin, y, regular, "Descripción", safe(puesto.getDescripcion()));
                    y = drawField(content, margin, y, regular, "Salario",
                            "$" + (puesto.getSalario() != null ? puesto.getSalario().toPlainString() : ""));
                    y = drawField(content, margin, y, regular, "Tipo de publicación", safe(puesto.getTipoPublicacion()));
                    y = drawField(content, margin, y, regular, "Estado",
                            Boolean.TRUE.equals(puesto.getActivo()) ? "Activo" : "Inactivo");

                    // Si tiene empresa, escribir su correo
                    if (puesto.getEmpresa() != null && puesto.getEmpresa().getUsuario() != null) {
                        y = drawField(content, margin, y, regular, "Empresa",
                                safe(puesto.getEmpresa().getUsuario().getCorreo()));
                    }

                    // Si tiene fecha, escribirla
                    if (puesto.getFechaRegistro() != null) {
                        y = drawField(content, margin, y, regular, "Fecha de publicación",
                                puesto.getFechaRegistro().format(formatter));
                    }

                    y -= 10;
                    contador++;
                }
            }

            // Dibujar pie de pagina en la ultima pagina
            drawFooter(content, margin, pageWidth, italic);
            content.close();

            // Guardar documento en el output stream
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF del reporte", e);
        }
    }

    // Dibuja el encabezado del PDF con fondo oscuro, titulo y subtitulo
    private void drawHeader(PDPageContentStream content, float pageWidth, float pageHeight,
                            float margin, PDType1Font bold, PDType1Font regular,
                            String nombreMes, int anio) throws IOException {
        // Dibujar rectangulo de fondo oscuro
        content.setNonStrokingColor(33f / 255f, 37f / 255f, 41f / 255f);
        content.addRect(0, pageHeight - 82, pageWidth, 82);
        content.fill();

        // Escribir titulo principal
        content.beginText();
        content.setNonStrokingColor(1f, 1f, 1f);
        content.setFont(bold, 20);
        content.newLineAtOffset(margin, pageHeight - 40);
        content.showText("Bolsa de Empleo");
        content.endText();

        // Escribir subtitulo con periodo
        content.beginText();
        content.setNonStrokingColor(1f, 1f, 1f);
        content.setFont(regular, 11);
        content.newLineAtOffset(margin, pageHeight - 58);
        content.showText("Reporte de puestos publicados - " + nombreMes + " " + anio);
        content.endText();

        // Dibujar linea separadora
        content.setStrokingColor(220f / 255f, 220f / 255f, 220f / 255f);
        content.moveTo(margin, pageHeight - 98);
        content.lineTo(pageWidth - margin, pageHeight - 98);
        content.stroke();
    }

    // Dibuja el pie de pagina del PDF
    private void drawFooter(PDPageContentStream content, float margin,
                            float pageWidth, PDType1Font italic) throws IOException {
        // Dibujar linea separadora superior del pie
        content.setStrokingColor(200f / 255f, 200f / 255f, 200f / 255f);
        content.moveTo(margin, 60);
        content.lineTo(pageWidth - margin, 60);
        content.stroke();

        // Escribir nombre del sistema
        content.beginText();
        content.setNonStrokingColor(100f / 255f, 100f / 255f, 100f / 255f);
        content.setFont(italic, 9);
        content.newLineAtOffset(margin, 42);
        content.showText("Sistema Bolsa de Empleo");
        content.endText();
    }

    // Dibuja un titulo de seccion en el PDF
    private float drawSectionTitle(PDPageContentStream content, float margin, float y,
                                   PDType1Font bold, String title) throws IOException {
        content.beginText();
        content.setNonStrokingColor(33f / 255f, 37f / 255f, 41f / 255f);
        content.setFont(bold, 13);
        content.newLineAtOffset(margin, y);
        content.showText(recortar(title));
        content.endText();
        return y - 18;
    }

    // Dibuja un campo label-valor en el PDF
    private float drawField(PDPageContentStream content, float margin, float y,
                            PDType1Font font, String label, String value) throws IOException {
        // Escribir label en negrita
        content.beginText();
        content.setNonStrokingColor(55f / 255f, 55f / 255f, 55f / 255f);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + 12, y);
        content.showText(recortar(label + ":"));
        content.endText();

        // Escribir valor
        content.beginText();
        content.setNonStrokingColor(0f, 0f, 0f);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + 110, y);
        content.showText(recortar(value));
        content.endText();

        return y - 15;
    }

    // Escribe una linea de texto simple en el PDF
    private float writeLine(PDPageContentStream content, String text, float x, float y,
                            PDType1Font font, int size) throws IOException {
        content.beginText();
        content.setNonStrokingColor(0f, 0f, 0f);
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(recortar(text));
        content.endText();
        return y - 16;
    }

    // Recorta el texto para evitar saltos de linea en el PDF.
    // Reemplaza saltos de linea y retorno de carro por espacios.
    private String recortar(String texto) {
        if (texto == null) return "";
        return texto.replace("\n", " ").replace("\r", " ");
    }

    // Wrapper seguro que retorna cadena vacia si el valor es null
    private String safe(String valor) {
        return valor == null ? "" : valor;
    }

    // Convierte el numero de mes a su nombre en espanol
    private String nombreMes(int mes) {
        return switch (mes) {
            case 1  -> "Enero";
            case 2  -> "Febrero";
            case 3  -> "Marzo";
            case 4  -> "Abril";
            case 5  -> "Mayo";
            case 6  -> "Junio";
            case 7  -> "Julio";
            case 8  -> "Agosto";
            case 9  -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "Mes inválido";
        };
    }
}
