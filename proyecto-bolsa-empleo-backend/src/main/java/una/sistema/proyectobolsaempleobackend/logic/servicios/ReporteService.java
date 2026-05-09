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

@Service
public class ReporteService {
    @Autowired
    private PuestoService puestoService;

    public byte[] generarPdfPuestosPorMesYAnio(int mes, int anio) {
        YearMonth yearMonth = YearMonth.of(anio, mes);

        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Puesto> puestos = puestoService.findPorFechaRegistroEntre(inicio, fin);
        String nombreMes = nombreMes(mes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font italic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth  = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();
            float margin = 48;

            PDPageContentStream content = new PDPageContentStream(document, page);

            drawHeader(content, pageWidth, pageHeight, margin, bold, regular, nombreMes, anio);

            float y = pageHeight - 125;

            y = writeLine(content, "Resumen general", margin, y, bold, 13);
            y = writeLine(content, "Total de puestos encontrados: " + puestos.size(), margin, y, regular, 11);
            y -= 12;

            if (puestos.isEmpty()) {
                writeLine(content, "No se encontraron puestos en el período seleccionado.", margin, y, regular, 11);
            } else {
                int contador = 1;

                for (Puesto puesto : puestos) {
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

                    y = drawSectionTitle(content, margin, y, bold, "Puesto " + contador);

                    y = drawField(content, margin, y, regular, "Descripción", safe(puesto.getDescripcion()));
                    y = drawField(content, margin, y, regular, "Salario",
                            "$" + (puesto.getSalario() != null ? puesto.getSalario().toPlainString() : ""));
                    y = drawField(content, margin, y, regular, "Tipo de publicación", safe(puesto.getTipoPublicacion()));
                    y = drawField(content, margin, y, regular, "Estado",
                            Boolean.TRUE.equals(puesto.getActivo()) ? "Activo" : "Inactivo");

                    if (puesto.getEmpresa() != null && puesto.getEmpresa().getUsuario() != null) {
                        y = drawField(content, margin, y, regular, "Empresa",
                                safe(puesto.getEmpresa().getUsuario().getCorreo()));
                    }

                    if (puesto.getFechaRegistro() != null) {
                        y = drawField(content, margin, y, regular, "Fecha de publicación",
                                puesto.getFechaRegistro().format(formatter));
                    }

                    y -= 10;
                    contador++;
                }
            }

            drawFooter(content, margin, pageWidth, italic);
            content.close();

            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF del reporte", e);
        }
    }

    private void drawHeader(PDPageContentStream content, float pageWidth, float pageHeight,
                            float margin, PDType1Font bold, PDType1Font regular,
                            String nombreMes, int anio) throws IOException {

        content.setNonStrokingColor(33f / 255f, 37f / 255f, 41f / 255f);
        content.addRect(0, pageHeight - 82, pageWidth, 82);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(1f, 1f, 1f);
        content.setFont(bold, 20);
        content.newLineAtOffset(margin, pageHeight - 40);
        content.showText("Bolsa de Empleo");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(1f, 1f, 1f);
        content.setFont(regular, 11);
        content.newLineAtOffset(margin, pageHeight - 58);
        content.showText("Reporte de puestos publicados - " + nombreMes + " " + anio);
        content.endText();

        content.setStrokingColor(220f / 255f, 220f / 255f, 220f / 255f);
        content.moveTo(margin, pageHeight - 98);
        content.lineTo(pageWidth - margin, pageHeight - 98);
        content.stroke();
    }

    private void drawFooter(PDPageContentStream content, float margin,
                            float pageWidth, PDType1Font italic) throws IOException {

        content.setStrokingColor(200f / 255f, 200f / 255f, 200f / 255f);
        content.moveTo(margin, 60);
        content.lineTo(pageWidth - margin, 60);
        content.stroke();

        content.beginText();
        content.setNonStrokingColor(100f / 255f, 100f / 255f, 100f / 255f);
        content.setFont(italic, 9);
        content.newLineAtOffset(margin, 42);
        content.showText("Sistema Bolsa de Empleo");
        content.endText();
    }

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

    private float drawField(PDPageContentStream content, float margin, float y,
                            PDType1Font font, String label, String value) throws IOException {
        content.beginText();
        content.setNonStrokingColor(55f / 255f, 55f / 255f, 55f / 255f);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + 12, y);
        content.showText(recortar(label + ":"));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(0f, 0f, 0f);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + 110, y);
        content.showText(recortar(value));
        content.endText();

        return y - 15;
    }

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

    private String recortar(String texto) {
        if (texto == null) return "";
        return texto.replace("\n", " ").replace("\r", " ");
    }

    private String safe(String valor) {
        return valor == null ? "" : valor;
    }

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
