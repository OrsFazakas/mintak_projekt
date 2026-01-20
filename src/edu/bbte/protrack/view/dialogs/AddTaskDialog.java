package edu.bbte.protrack.view.dialogs;

import edu.bbte.protrack.logic.creators.MarketingProjectFactory;
import edu.bbte.protrack.logic.creators.SalesProjectFactory;
import edu.bbte.protrack.logic.creators.ITProjectFactory;
import edu.bbte.protrack.logic.creators.ProjectFactory;
import edu.bbte.protrack.model.entities.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


//Dialógus, amely az Abstract Factory segítségével hoz létre típus-specifikus feladatokat.

public class AddTaskDialog extends JDialog {
    private JTextField nameField;
    private JTextField costField;
    private JComboBox<String> typeCombo;
    private JComboBox<Task.Priority> priorityCombo;
    private JTextField deadlineField;
    private JTextField employeeField;
    private Task resultTask;

    public AddTaskDialog(Frame owner) {
        super(owner, "Új feladat hozzáadása", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Név
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Feladat neve:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        add(nameField, gbc);

        // Típus
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Típus:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[] { "IT Fejlesztés", "Marketing", "Sales" });
        add(typeCombo, gbc);

        // Költség
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Költség (€):"), gbc);
        gbc.gridx = 1;
        costField = new JTextField("1000");
        add(costField, gbc);

        // Prioritás
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Prioritás:"), gbc);
        gbc.gridx = 1;
        priorityCombo = new JComboBox<>(Task.Priority.values());
        priorityCombo.setSelectedItem(Task.Priority.MEDIUM);
        add(priorityCombo, gbc);

        // Határidő
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Határidő (ÉÉÉÉ-HH-NN):"), gbc);
        gbc.gridx = 1;
        deadlineField = new JTextField(LocalDate.now().plusWeeks(1).toString());
        add(deadlineField, gbc);

        // Felelős
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Felelős:"), gbc);
        gbc.gridx = 1;
        employeeField = new JTextField("Nincs hozzárendelve");
        add(employeeField, gbc);

        // Gombok
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton okButton = new JButton("Létrehozás");
        okButton.addActionListener(e -> createTask());
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Mégse");
        cancelButton.addActionListener(e -> {
            resultTask = null;
            setVisible(false);
        });
        buttonPanel.add(cancelButton);

        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    private void createTask() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A név megadása kötelező!");
                return;
            }

            double cost = Double.parseDouble(costField.getText());

            // Abstract Factory kiválasztása
            ProjectFactory factory;
            String selectedType = (String) typeCombo.getSelectedItem();
            switch (selectedType) {
                case "IT Fejlesztés":
                    factory = new ITProjectFactory();
                    break;
                case "Marketing":
                    factory = new MarketingProjectFactory();
                    break;
                case "Sales":
                    factory = new SalesProjectFactory();
                    break;
                default:
                    factory = new ITProjectFactory();
            }

            // A gyár hozza létre a konkrét Task-ot
            resultTask = factory.createTask(name, cost);

            // Prioritás beállítása
            resultTask.setPriority((Task.Priority) priorityCombo.getSelectedItem());

            // Határidő beállítása
            String deadlineStr = deadlineField.getText().trim();
            if (!deadlineStr.isEmpty()) {
                try {
                    LocalDate deadline = LocalDate.parse(deadlineStr);
                    resultTask.setDeadline(deadline);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Érvénytelen dátum formátum! (ÉÉÉÉ-HH-NN)");
                    return;
                }
            }

            // Felelős beállítása
            String employee = employeeField.getText().trim();
            if (!employee.isEmpty()) {
                resultTask.setAssignedEmployee(employee);
            }

            setVisible(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Érvénytelen költség formátum!");
        }
    }

    public Task getResultTask() {
        return resultTask;
    }
}
