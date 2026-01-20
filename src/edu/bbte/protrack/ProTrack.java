package edu.bbte.protrack;

import edu.bbte.protrack.view.main.MainWindow;
import javax.swing.*;

//Az alkalmazás belépési pontja.
public class ProTrack {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ha nem sikerül, marad az alap Swing kinézet (Metal/Nimbus)
            System.err.println("Nem sikerült betölteni a natív Look and Feel-t.");
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();

            // Az ablak középre helyezése
            mainWindow.setLocationRelativeTo(null);

            // Az ablak megjelenítése
            mainWindow.setVisible(true);
        });
    }
}