package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;

//Általános interfész üzleti szabályok (specifikációk) definiálásához.
public interface Specification {
    boolean isSatisfiedBy(ProjectComponent component);
}