package edu.bbte.protrack.view.dialogs;

import edu.bbte.protrack.logic.creators.ProjectBuilder;
import edu.bbte.protrack.model.entities.TaskGroup;

import javax.swing.*;
import java.awt.*;

//Dialógus ablak, amely a ProjectBuilder segítségével hoz létre új projektet.
public class ProjectWizardDialog extends JDialog {
    private JTextField nameField;
    private TaskGroup result;
    private boolean confirmed = false;

    public ProjectWizardDialog(Frame owner) {
        super(owner, "Új projekt varázsló", true);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Projekt neve:"));
        nameField = new JTextField();
        add(nameField);

        JButton createButton = new JButton("Létrehozás");
        createButton.addActionListener(e -> {
            if (!nameField.getText().isEmpty()) {
                // Itt használjuk a Builder mintát
                ProjectBuilder builder = new ProjectBuilder(nameField.getText());
                // Itt lehetne további konfiguráció (pl. határidő)
                result = builder.build();
                confirmed = true;
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "A név nem lehet üres!");
            }
        });

        add(createButton);
        setSize(300, 150);
        setLocationRelativeTo(owner);
    }

    public TaskGroup getResult() {
        return confirmed ? result : null;
    }
}