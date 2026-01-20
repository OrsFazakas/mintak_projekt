package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;

//Szabály, amely megvizsgálja, hogy egy elem költsége meghalad-e egy összeghatárt.
public class ExpensiveTaskSpecification implements Specification {
    private final double threshold;

    public ExpensiveTaskSpecification(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isSatisfiedBy(ProjectComponent component) {
        return component.getCalculateTotalCost() > threshold;
    }
}