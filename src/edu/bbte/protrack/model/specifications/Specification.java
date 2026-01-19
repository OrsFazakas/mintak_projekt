package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;

/**
 * Általános interfész üzleti szabályok (specifikációk) definiálásához.
 */
public interface Specification {
    /**
     * Eldönti, hogy az adott komponens megfelel-e a szabálynak.
     * @param component A vizsgált projekt elem (Task vagy TaskGroup).
     * @return true, ha megfelel a feltételnek.
     */
    boolean isSatisfiedBy(ProjectComponent component);
}