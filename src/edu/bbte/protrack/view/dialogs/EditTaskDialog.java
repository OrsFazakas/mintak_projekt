package edu.bbte.protrack.view.dialogs;

import edu.bbte.protrack.model.entities.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Dialógus egy meglévő feladat szerkesztéséhez.
 */
public class EditTaskDialog extends JDialog {
    private final Task task;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField costField;
    private JTextField employeeField;
    private JTextField deadlineField;
    private JComboBox<Task.Priority> priorityCombo;
    private JSlider completionSlider;

    public EditTaskDialog(Frame owner, Task task) {
        super(owner, "Feladat szerkesztése", true);
        this.task = task;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Név
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Név:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(task.getName(), 20);
        add(nameField, gbc);

        // Költség
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Költség (€):"), gbc);
        gbc.gridx = 1;
        costField = new JTextField(String.valueOf(task.getBaseCost()));
        add(costField, gbc);

        // Felelős
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Felelős:"), gbc);
        gbc.gridx = 1;
        employeeField = new JTextField(task.getAssignedEmployee());
        add(employeeField, gbc);

        // Prioritás
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Prioritás:"), gbc);
        gbc.gridx = 1;
        priorityCombo = new JComboBox<>(Task.Priority.values());
        priorityCombo.setSelectedItem(task.getPriority());
        add(priorityCombo, gbc);

        // Határidő
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Határidő (ÉÉÉÉ-HH-NN):"), gbc);
        gbc.gridx = 1;
        String deadlineStr = task.getDeadline() != null ? task.getDeadline().toString() : "";
        deadlineField = new JTextField(deadlineStr);
        add(deadlineField, gbc);

        // Haladás
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel("Haladás: " + task.getCompletionPercentage() + "%"), gbc);
        gbc.gridx = 1;
        completionSlider = new JSlider(0, 100, task.getCompletionPercentage());
        completionSlider.setMajorTickSpacing(25);
        completionSlider.setPaintTicks(true);
        completionSlider.setPaintLabels(true);
        add(completionSlider, gbc);

        // Gombok
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveButton = new JButton("💾 Mentés");
        saveButton.addActionListener(e -> saveChanges());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Mégse");
        cancelButton.addActionListener(e -> {
            saved = false;
            setVisible(false);
        });
        buttonPanel.add(cancelButton);

        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    private void saveChanges() {
        try {
            // Név
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A név megadása kötelező!");
                return;
            }

            // Költség
            double cost = Double.parseDouble(costField.getText());

            // Felelős
            String employee = employeeField.getText().trim();

            // Prioritás
            Task.Priority priority = (Task.Priority) priorityCombo.getSelectedItem();

            // Határidő
            LocalDate deadline = null;
            String deadlineStr = deadlineField.getText().trim();
            if (!deadlineStr.isEmpty()) {
                try {
                    deadline = LocalDate.parse(deadlineStr);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Érvénytelen dátum formátum! (ÉÉÉÉ-HH-NN)");
                    return;
                }
            }

            // Haladás
            int completion = completionSlider.getValue();

            // Mentés a task-ba
            task.setName(name);
            task.setBaseCost(cost);
            task.setAssignedEmployee(employee);
            task.setPriority(priority);
            task.setDeadline(deadline);
            task.setCompletion(completion);

            saved = true;
            setVisible(false);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Érvénytelen költség formátum!");
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
