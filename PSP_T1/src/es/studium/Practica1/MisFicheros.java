package es.studium.Practica1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MisFicheros extends JFrame {

    private JTextArea resultArea;
    private JTextField searchField;
    private JButton searchButton;

    public MisFicheros() {
        // Configuración de la ventana principal
        setTitle("Mis Ficheros");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel para el área de texto de resultados y campo de búsqueda
        JPanel panel = new JPanel(new BorderLayout());

        // Área de resultados
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Campo de texto y botón de búsqueda
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(".exe", 20); // Campo por defecto busca archivos .exe
        searchButton = new JButton("Buscar");

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.SOUTH);

        add(panel);

        // Acción del botón "Buscar"
        searchButton.addActionListener(e -> {
            // Ejecutar la búsqueda en un hilo separado para no congelar la interfaz
            new Thread(() -> searchFiles(searchField.getText())).start();
        });

        // Acción de doble clic en el área de resultados para ejecutar archivos .exe
        resultArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int position = resultArea.viewToModel2D(e.getPoint());
                    try {
                        // Obtener la línea donde se hizo clic
                        int start = resultArea.getLineStartOffset(resultArea.getLineOfOffset(position));
                        int end = resultArea.getLineEndOffset(resultArea.getLineOfOffset(position));
                        String filePath = resultArea.getText().substring(start, end).trim();

                        // Si es un archivo ejecutable, intentar ejecutarlo
                        if (filePath.endsWith(".exe")) {
                            executeFile(filePath);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // Método para buscar archivos en todas las unidades
    private void searchFiles(String extension) {
        resultArea.setText(""); // Limpiar el área de resultados
        if (extension.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una extensión de archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ArrayList<File> drives = new ArrayList<>();
        File[] roots = File.listRoots(); // Obtener todas las unidades
        for (File root : roots) {
            drives.add(root);
        }

        for (File drive : drives) {
            searchInDirectory(drive, extension);
        }
    }

    // Método recursivo para buscar archivos en un directorio
    private void searchInDirectory(File directory, String extension) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        if (file.isDirectory()) {
                            searchInDirectory(file, extension); // Llamada recursiva para buscar en subdirectorios
                        } else {
                            if (file.getName().endsWith(extension)) {
                                resultArea.append(file.getAbsolutePath() + "\n"); // Mostrar el archivo encontrado
                            }
                        }
                    } catch (SecurityException e) {
                        System.out.println("No se puede acceder a: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    // Método para ejecutar un archivo .exe
    private void executeFile(String filePath) {
        try {
            Runtime.getRuntime().exec(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al intentar ejecutar el archivo: " + filePath, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MisFicheros app = new MisFicheros();
            app.setVisible(true);
        });
    }
}
