package edu.bbte.protrack.view.dialogs;

import edu.bbte.protrack.logic.creators.ConstructionProjectFactory;
import edu.bbte.protrack.logic.creators.ITProjectFactory;
import edu.bbte.protrack.logic.creators.ProjectFactory;
import edu.bbte.protrack.model.entities.Task;

import javax.swing.*;
import java.awt.*;

/**
 * Dialógus, amely az Abstract Factory segítségével hoz létre típus-specifikus feladatokat.
 */
public class AddTaskDialog extends JDialog {
    private JTextField nameField;
    private JTextField costField;
    private JComboBox<String> typeCombo;
    private Task resultTask;

    public AddTaskDialog(Frame owner) {
        super(owner, "Új feladat hozzáadása", true);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Feladat neve:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Alapköltség (€):"));
        costField = new JTextField();
        add(costField);

        add(new JLabel("Típus:"));
        typeCombo = new JComboBox<>(new String[]{"IT Fejlesztés", "Építőipar"});
        add(typeCombo);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double cost = Double.parseDouble(costField.getText());

                // Abstract Factory kiválasztása
                ProjectFactory factory;
                if (typeCombo.getSelectedItem().equals("IT Fejlesztés")) {
                    factory = new ITProjectFactory();
                } else {
                    factory = new ConstructionProjectFactory();
                }

                // A gyár hozza létre a konkrét Task-ot
                resultTask = factory.createTask(name, cost);
                setVisible(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Érvénytelen költség formátum!");
            }
        });

        add(okButton);
        pack();
        setLocationRelativeTo(owner);
    }

    public Task getResultTask() {
        return resultTask;
    }
}