package edu.bbte.protrack.view.components;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.specifications.ExpensiveTaskSpecification;
import edu.bbte.protrack.model.specifications.IncompleteSpecification;
import edu.bbte.protrack.model.specifications.Specification;
import edu.bbte.protrack.observer.ProjectEvent;
import edu.bbte.protrack.observer.ProjectObserver;

import javax.swing.*;
import java.awt.*;

/**
 * Statisztikai panel, amely az Observer mintán keresztül frissül,
 * és a Specification minta segítségével szűri az adatokat.
 */
public class StatisticsPanel extends JPanel implements ProjectObserver {
    private TaskGroup rootProject;
    private ProjectComponent selectedComponent;
    private JLabel totalCostLabel;
    private JLabel expensiveTasksLabel;
    private JLabel incompleteTasksLabel;
    private JLabel contextLabel;

    public StatisticsPanel(TaskGroup rootProject) {
        this.rootProject = rootProject;
        this.selectedComponent = rootProject;
        setLayout(new GridLayout(0, 1, 5, 10));
        setBorder(BorderFactory.createTitledBorder("Projekt Statisztika"));
        setPreferredSize(new Dimension(220, 0));

        contextLabel = new JLabel("📊 Teljes projekt");
        contextLabel.setFont(contextLabel.getFont().deriveFont(Font.BOLD));
        totalCostLabel = new JLabel("Összköltség: 0 €");
        expensiveTasksLabel = new JLabel("Kritikus (drága) feladatok: 0");
        incompleteTasksLabel = new JLabel("Folyamatban lévő feladatok: 0");

        add(contextLabel);
        add(Box.createVerticalStrut(10));
        add(totalCostLabel);
        add(Box.createVerticalStrut(20));
        add(expensiveTasksLabel);
        add(Box.createVerticalStrut(20));
        add(incompleteTasksLabel);

        updateStatistics();
    }

    /**
     * Frissíti a gyökér projektet (betöltés után).
     */
    public void updateRootProject(TaskGroup newRoot) {
        this.rootProject = newRoot;
        this.selectedComponent = newRoot;
        updateStatistics();
    }

    /**
     * Beállítja a kiválasztott elemet a kontextus-érzékeny statisztikákhoz.
     */
    public void setSelectedComponent(ProjectComponent component) {
        this.selectedComponent = (component != null) ? component : rootProject;
        updateStatistics();
    }

    /**
     * Kiszámolja a statisztikákat a Specification minta használatával.
     */
    private void updateStatistics() {
        if (rootProject == null)
            return;

        // Kontextus megjelenítése
        if (selectedComponent instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) selectedComponent;
            contextLabel.setText("📊 " + group.getName());

            // 1. Összköltség lekérése a Composite struktúrából
            double totalCost = group.getCalculateTotalCost();
            totalCostLabel.setText(String.format("Összköltség: %.2f €", totalCost));

            // 2. Drága feladatok szűrése Specification-nel (pl. 5000 € felett)
            Specification expensiveSpec = new ExpensiveTaskSpecification(5000);
            long expensiveCount = countMatches(group, expensiveSpec);
            expensiveTasksLabel.setText("Kritikus (drága) feladatok: " + expensiveCount);

            // 3. Befejezetlen feladatok szűrése (csak a kiválasztott csoportban)
            Specification incompleteSpec = new IncompleteSpecification();
            long incompleteCount = countMatches(group, incompleteSpec);
            incompleteTasksLabel.setText("Folyamatban lévő feladatok: " + incompleteCount);
        } else if (selectedComponent instanceof Task) {
            Task task = (Task) selectedComponent;
            contextLabel.setText("📋 " + task.getName());
            totalCostLabel.setText(String.format("Költség: %.2f €", task.getBaseCost()));
            expensiveTasksLabel.setText("Haladás: " + task.getCompletionPercentage() + "%");
            incompleteTasksLabel.setText(task.isCompleted() ? "✅ Befejezve" : "⏳ Folyamatban");
        }
    }

    /**
     * Segédmetódus, amely rekurzívan megszámolja a specifikációnak megfelelő
     * elemeket.
     */
    private long countMatches(TaskGroup group, Specification spec) {
        long count = 0;
        for (ProjectComponent comp : group.getChildren()) {
            if (comp instanceof Task && spec.isSatisfiedBy(comp)) {
                count++;
            }
            if (comp instanceof TaskGroup) {
                count += countMatches((TaskGroup) comp, spec);
            }
        }
        return count;
    }

    @Override
    public void onProjectChanged(ProjectEvent event) {
        // Amikor az Observer jelez, újra számolunk mindent
        updateStatistics();
        revalidate();
        repaint();
    }
}