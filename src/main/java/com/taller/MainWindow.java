package com.taller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private final JTextField hostField = new JTextField("localhost", 20);
    private final JTextField portField = new JTextField("3306", 6);
    private final JTextField dbField = new JTextField("unillanos_admitidos", 20);
    private final JTextField userField = new JTextField("root", 10);
    private final JPasswordField passField = new JPasswordField("", 10);
    private final JTextField outField = new JTextField("reporte_admisiones.pdf", 20);
    private final JLabel statusLabel = new JLabel("Listo");

    public MainWindow() {
        super("Generador de Reportes PDF - Unillanos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        form.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(hostField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(portField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Base de datos:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(dbField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(userField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(passField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Archivo salida:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(outField, gbc);

        main.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton genBtn = new JButton("Generar PDF");
        JButton closeBtn = new JButton("Cerrar");
        buttons.add(statusLabel);
        buttons.add(genBtn);
        buttons.add(closeBtn);
        main.add(buttons, BorderLayout.SOUTH);

        genBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePdf();
            }
        });

        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        getContentPane().add(main);
    }

    private void generatePdf() {
        final String host = hostField.getText().trim();
        final int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Puerto inválido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String db = dbField.getText().trim();
        final String user = userField.getText().trim();
        final String pass = new String(passField.getPassword());
        final String salida = outField.getText().trim();

        statusLabel.setText("Generando...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ReportesPDF gen = new ReportesPDF(host, port, db, user, pass);
                gen.generar(salida);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    statusLabel.setText("Listo");
                    JOptionPane.showMessageDialog(MainWindow.this, "PDF generado: " + salida, "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    statusLabel.setText("Error");
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Error generando PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow w = new MainWindow();
            w.setVisible(true);
        });
    }
}
