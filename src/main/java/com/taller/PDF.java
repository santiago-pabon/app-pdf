package com.taller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.IOException;

public class PDF {
    private PDDocument documento;
    private String nombreArchivo;

    public PDF(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
        this.documento = new PDDocument();
    }

    public void agregarPagina(String texto) throws IOException {
        PDPage pagina = new PDPage();
        documento.addPage(pagina);

        PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA, 12);
        contenido.newLineAtOffset(50, 700);
        contenido.showText(texto);
        contenido.endText();
        contenido.close();
    }

    public void guardar() throws IOException {
        documento.save(nombreArchivo);
        System.out.println("PDF guardado exitosamente: " + nombreArchivo);
    }

    public void cerrar() throws IOException {
        if (documento != null) {
            documento.close();
        }
    }
}
