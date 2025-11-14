package com.taller;

public class PDFAplicacion {
    public static void main(String[] args) {
        String salida = args != null && args.length > 0 ? args[0] : "reporte_admisiones.pdf";
        try {
            ReportesPDF gen = new ReportesPDF("localhost", 3306, "unillanos_admisiones", "root", "1234");
            gen.generar(salida);
            System.out.println("PDF generado: " + salida);
        } catch (Exception e) {
            System.err.println("Error generando el PDF de reportes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
