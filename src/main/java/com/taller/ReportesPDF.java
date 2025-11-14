package com.taller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportesPDF {
    private final String jdbcUrl;
    private final String user;
    private final String pass;

    public ReportesPDF(String host, int port, String database, String user, String pass) {
        this.jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true",
                host, port, database);
        this.user = user;
        this.pass = pass;
    }

    public void generar(String salidaPDF) throws Exception {
        try (Connection con = DriverManager.getConnection(jdbcUrl, user, pass);
                PDDocument doc = new PDDocument()) {

            // Titular del documento
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40f;
            float y = page.getMediaBox().getHeight() - margin;

            y = drawTitle(cs, "Reportes de Admisiones Unillanos", y);
            y = drawSubtitle(cs, "Fecha: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), y);
            cs.close();

            // Reporte 1: Cantidad admitidos por programa
            List<String[]> r1 = consultaAdmitidosPorPrograma(con);
            page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - margin;
            y = drawSectionTitle(cs, "Reporte #1: Cantidad admitidos por programa", y);
            cs.close();
            y -= 8;
            drawTable(doc, page, y,
                    new String[] { "Programa", "Total" },
                    r1,
                    new float[] { 430f, 100f });

            // Reporte 2: Cantidad H/M por facultad
            List<String[]> r2 = consultaGeneroPorFacultad(con);
            page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - margin;
            y = drawSectionTitle(cs, "Reporte #2: Hombres y mujeres por facultad", y);
            cs.close();
            y -= 8;
            drawTable(doc, page, y,
                    new String[] { "Facultad", "Hombres", "Mujeres", "Total" },
                    r2,
                    new float[] { 330f, 70f, 70f, 70f });

            doc.save(salidaPDF);
        }
    }

    private List<String[]> consultaAdmitidosPorPrograma(Connection con) throws SQLException {
        String sql = "SELECT programa, COUNT(*) AS total FROM admitidos GROUP BY programa ORDER BY total DESC, programa";
        List<String[]> rows = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String prog = rs.getString("programa");
                int total = rs.getInt("total");
                rows.add(new String[] { prog == null ? "" : prog, Integer.toString(total) });
            }
        }
        return rows;
    }

    private List<String[]> consultaGeneroPorFacultad(Connection con) throws SQLException {
        String sql = "SELECT facultad, " +
                "SUM(CASE WHEN genero='MASCULINO' THEN 1 ELSE 0 END) AS hombres, " +
                "SUM(CASE WHEN genero='FEMENINO'  THEN 1 ELSE 0 END) AS mujeres " +
                "FROM admitidos GROUP BY facultad ORDER BY facultad";
        List<String[]> rows = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String fac = rs.getString("facultad");
                int h = rs.getInt("hombres");
                int m = rs.getInt("mujeres");
                rows.add(new String[] { fac == null ? "" : fac, Integer.toString(h), Integer.toString(m),
                        Integer.toString(h + m) });
            }
        }
        return rows;
    }

    // ===== Helpers de dibujo =====

    private float drawTitle(PDPageContentStream cs, String text, float y) throws IOException {
        return drawTextLine(cs, text, 40f, y, PDType1Font.HELVETICA_BOLD, 20f) - 10f;
    }

    private float drawSubtitle(PDPageContentStream cs, String text, float y) throws IOException {
        return drawTextLine(cs, text, 40f, y, PDType1Font.HELVETICA, 12f) - 10f;
    }

    private float drawSectionTitle(PDPageContentStream cs, String text, float y) throws IOException {
        return drawTextLine(cs, text, 40f, y, PDType1Font.HELVETICA_BOLD, 14f) - 6f;
    }

    private float drawTextLine(PDPageContentStream cs, String text, float x, float y, PDType1Font font, float size)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
        return y - size;
    }

    private void drawTable(PDDocument doc, PDPage page, float yStart,
            String[] headers, List<String[]> rows, float[] colWidths) throws IOException {
        float margin = 40f;
        float tableX = margin;
        float rowH = 16f;
        float headerSize = 11f;
        float cellSize = 10f;
        PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font cellFont = PDType1Font.HELVETICA;

        float y = yStart;
        PDPage currentPage = page;
        PDPageContentStream cs = new PDPageContentStream(doc, currentPage, PDPageContentStream.AppendMode.APPEND, true);

        // Dibuja header inicial
        float x = tableX;
        for (int i = 0; i < headers.length; i++) {
            drawCellText(cs, headers[i], x, y, colWidths[i], headerFont, headerSize);
            x += colWidths[i];
        }
        y -= rowH;

        for (String[] r : rows) {
            // Si no hay espacio para otra fila, nueva página y redibujo header
            if (y <= margin + rowH) {
                cs.close();
                currentPage = new PDPage(PDRectangle.LETTER);
                doc.addPage(currentPage);
                cs = new PDPageContentStream(doc, currentPage);
                y = currentPage.getMediaBox().getHeight() - margin;
                // Redibuja header en la nueva página
                float xh = tableX;
                for (int i = 0; i < headers.length; i++) {
                    drawCellText(cs, headers[i], xh, y, colWidths[i], headerFont, headerSize);
                    xh += colWidths[i];
                }
                y -= rowH;
            }

            x = tableX;
            for (int i = 0; i < headers.length; i++) {
                String val = i < r.length ? r[i] : "";
                drawCellText(cs, val, x, y, colWidths[i], cellFont, cellSize);
                x += colWidths[i];
            }
            y -= rowH;
        }

        cs.close();
    }

    private void drawCellText(PDPageContentStream cs, String text, float x, float y, float width,
            PDType1Font font, float fontSize) throws IOException {
        String fitted = fitText(font, fontSize, text == null ? "" : text, width - 4f);
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x + 2f, y);
        cs.showText(fitted);
        cs.endText();
    }

    private String fitText(PDType1Font font, float size, String text, float maxWidth) throws IOException {
        if (text == null)
            return "";
        float w = font.getStringWidth(text) / 1000f * size;
        if (w <= maxWidth)
            return text;
        String ellipsis = "…";
        for (int i = text.length() - 1; i >= 0; i--) {
            String cand = text.substring(0, i) + ellipsis;
            float cw = font.getStringWidth(cand) / 1000f * size;
            if (cw <= maxWidth)
                return cand;
        }
        return ellipsis;
    }

    // (el paginado de tablas se maneja dentro de drawTable)

    public static void main(String[] args) throws Exception {
        // Config DB
        String host = "localhost";
        int port = 3306;
        String db = "unillanos_admitidos";
        String user = "root";
        String pass = "";

        String salida = "reporte_admisiones.pdf";
        if (args.length > 0)
            salida = args[0];

        ReportesPDF gen = new ReportesPDF(host, port, db, user, pass);
        gen.generar(salida);
        System.out.println("PDF generado: " + salida);
    }
}
