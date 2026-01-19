package edu.bbte.protrack.view.components;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.specifications.ExpensiveTaskSpecification;
import edu.bbte.protrack.model.specifications.IncompleteSpecification;
import edu.bbte.protrack.model.specifications.Specification;
import edu.bbte.protrack.observer.ProjectEvent;
import edu.bbte.protrack.observer.ProjectObserver;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statisztikai panel, amely az Observer mintán keresztül frissül,
 * és a Specification minta segítségével szűri az adatokat.
 */
public class StatisticsPanel extends JPanel implements ProjectObserver {
    private final TaskGroup rootProject;
    private JLabel totalCostLabel;
    private JLabel expensiveTasksLabel;
    private JLabel incompleteTasksLabel;

    public StatisticsPanel(TaskGroup rootProject) {
        this.rootProject = rootProject;
        setLayout(new GridLayout(3, 1, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Projekt Statisztika"));

        totalCostLabel = new JLabel("Összköltség: 0 €");
        expensiveTasksLabel = new JLabel("Kritikus (drága) feladatok: 0");
        incompleteTasksLabel = new JLabel("Folyamatban lévő feladatok: 0");

        add(totalCostLabel);
        add(expensiveTasksLabel);
        add(incompleteTasksLabel);

        updateStatistics();
    }

    /**
     * Kiszámolja a statisztikákat a Specification minta használatával.
     */
    private void updateStatistics() {
        // 1. Összköltség lekérése a Composite struktúrából
        double totalCost = rootProject.getCalculateTotalCost();
        totalCostLabel.setText(String.format("Összköltség: %.2f €", totalCost));

        // 2. Drága feladatok szűrése Specification-nel (pl. 5000 € felett)
        Specification expensiveSpec = new ExpensiveTaskSpecification(5000);
        long expensiveCount = countMatches(rootProject, expensiveSpec);
        expensiveTasksLabel.setText("Kritikus (drága) feladatok: " + expensiveCount);

        // 3. Befejezetlen feladatok szűrése
        Specification incompleteSpec = new IncompleteSpecification();
        long incompleteCount = countMatches(rootProject, incompleteSpec);
        incompleteTasksLabel.setText("Folyamatban lévő feladatok: " + incompleteCount);
    }

    /**
     * Segédmetódus, amely rekurzívan megszámolja a specifikációnak megfelelő elemeket.
     */
    private long countMatches(TaskGroup group, Specification spec) {
        long count = 0;
        for (ProjectComponent comp : group.getChildren()) {
            if (spec.isSatisfiedBy(comp)) {
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