package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;

/**
 * Szabály, amely a be nem fejezett elemeket szűri ki.
 */
public class IncompleteSpecification implements Specification {
    @Override
    public boolean isSatisfiedBy(ProjectComponent component) {
        return component.getCompletionPercentage() < 100;
    }
}