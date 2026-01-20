package edu.bbte.protrack.view.components;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.specifications.ExpensiveTaskSpecification;
import edu.bbte.protrack.model.specifications.IncompleteSpecification;
import edu.bbte.protrack.model.specifications.OverdueSpecification;
import edu.bbte.protrack.model.specifications.DueSoonSpecification;
import edu.bbte.protrack.model.specifications.AndSpecification;
import edu.bbte.protrack.model.specifications.OrSpecification;
import edu.bbte.protrack.model.specifications.Specification;
import edu.bbte.protrack.logic.services.TaxDecorator;
import edu.bbte.protrack.observer.ProjectEvent;
import edu.bbte.protrack.observer.ProjectObserver;

import javax.swing.*;
import java.awt.*;

/**
 * Statisztikai panel, amely az Observer mintán keresztül frissül,
 * és a Specification minta segítségével szűri az adatokat.
 * A Decorator minta segítségével ÁFA-val növelt költséget is megjelenít.
 */
public class StatisticsPanel extends JPanel implements ProjectObserver {
    private TaskGroup rootProject;
    private ProjectComponent selectedComponent;
    private JLabel totalCostLabel;
    private JLabel costWithTaxLabel;
    private JLabel expensiveTasksLabel;
    private JLabel incompleteTasksLabel;
    private JLabel urgentTasksLabel;
    private JLabel watchListLabel;
    private JLabel contextLabel;

    private static final double TAX_RATE = 0.27; // 27% ÁFA

    public StatisticsPanel(TaskGroup rootProject) {
        this.rootProject = rootProject;
        this.selectedComponent = rootProject;
        setLayout(new GridLayout(0, 1, 5, 6));
        setBorder(BorderFactory.createTitledBorder("Statisztika"));
        setPreferredSize(new Dimension(200, 0));

        contextLabel = new JLabel("📊 Teljes projekt");
        contextLabel.setFont(contextLabel.getFont().deriveFont(Font.BOLD));
        totalCostLabel = new JLabel("Nettó: 0 €");
        costWithTaxLabel = new JLabel("Bruttó: 0 €");
        costWithTaxLabel.setForeground(new Color(0, 100, 180));
        expensiveTasksLabel = new JLabel("Drága: 0");
        incompleteTasksLabel = new JLabel("Folyamatban: 0");
        urgentTasksLabel = new JLabel("🚨 Sürgős: 0");
        urgentTasksLabel.setForeground(Color.RED);
        watchListLabel = new JLabel("⚠️ Figyelendő: 0");
        watchListLabel.setForeground(new Color(200, 100, 0));

        add(contextLabel);
        add(totalCostLabel);
        add(costWithTaxLabel);
        add(Box.createVerticalStrut(3));
        add(expensiveTasksLabel);
        add(incompleteTasksLabel);
        add(urgentTasksLabel);
        add(watchListLabel);

        updateStatistics();
    }

    public void updateRootProject(TaskGroup newRoot) {
        this.rootProject = newRoot;
        this.selectedComponent = newRoot;
        updateStatistics();
    }

    public void setSelectedComponent(ProjectComponent component) {
        this.selectedComponent = (component != null) ? component : rootProject;
        updateStatistics();
    }

    private void updateStatistics() {
        if (rootProject == null)
            return;

        if (selectedComponent instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) selectedComponent;
            contextLabel.setText("📊 " + group.getName());

            // Nettó költség
            double netCost = group.getCalculateTotalCost();
            totalCostLabel.setText(String.format("Nettó: %.0f €", netCost));

            // DECORATOR: Bruttó költség ÁFA-val
            TaxDecorator taxDecorated = new TaxDecorator(group, TAX_RATE);
            double grossCost = taxDecorated.getCalculateTotalCost();
            costWithTaxLabel.setText(String.format("Bruttó: %.0f €", grossCost));

            // SPECIFICATION: Drága feladatok
            Specification expensiveSpec = new ExpensiveTaskSpecification(5000);
            long expensiveCount = countMatches(group, expensiveSpec);
            expensiveTasksLabel.setText("Drága (>5000€): " + expensiveCount);

            // SPECIFICATION: Befejezetlen
            Specification incompleteSpec = new IncompleteSpecification();
            long incompleteCount = countMatches(group, incompleteSpec);
            incompleteTasksLabel.setText("Folyamatban: " + incompleteCount);

            // AND SPECIFICATION: Befejezetlen ÉS 2 napon belül lejár (sürgős)
            Specification dueSoonSpec = new DueSoonSpecification();
            Specification urgentSpec = new AndSpecification(incompleteSpec, dueSoonSpec);
            long urgentCount = countMatches(group, urgentSpec);
            urgentTasksLabel.setText("🚨 Sürgős: " + urgentCount);

            // OR SPECIFICATION: Drága VAGY késésben (figyelendő)
            Specification overdueSpec = new OverdueSpecification();
            Specification watchSpec = new OrSpecification(expensiveSpec, overdueSpec);
            long watchCount = countMatches(group, watchSpec);
            watchListLabel.setText("⚠️ Figyelendő: " + watchCount);

        } else if (selectedComponent instanceof Task) {
            Task task = (Task) selectedComponent;
            contextLabel.setText("📋 " + task.getName());

            double netCost = task.getBaseCost();
            totalCostLabel.setText(String.format("Nettó: %.0f €", netCost));

            TaxDecorator taxDecorated = new TaxDecorator(task, TAX_RATE);
            costWithTaxLabel.setText(String.format("Bruttó: %.0f €", taxDecorated.getCalculateTotalCost()));

            expensiveTasksLabel.setText("Haladás: " + task.getCompletionPercentage() + "%");
            incompleteTasksLabel.setText(task.isCompleted() ? "✅ Kész" : "⏳ Folyamatban");
            urgentTasksLabel.setText(task.isOverdue() ? "🚨 KÉSÉSBEN!" : "");
            watchListLabel.setText("");
        }
    }

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
        updateStatistics();
        revalidate();
        repaint();
    }
}