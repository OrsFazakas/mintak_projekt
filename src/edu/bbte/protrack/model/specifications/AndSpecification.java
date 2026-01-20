package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;

//Logikai ÉS kapcsolat két specifikáció között.
public class AndSpecification implements Specification {
    private final Specification left;
    private final Specification right;

    public AndSpecification(Specification left, Specification right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(ProjectComponent component) {
        return left.isSatisfiedBy(component) && right.isSatisfiedBy(component);
    }
}