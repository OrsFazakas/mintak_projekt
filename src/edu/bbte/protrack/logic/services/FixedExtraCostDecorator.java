package edu.bbte.protrack.logic.services;

import edu.bbte.protrack.model.entities.ProjectComponent;

/**
 * Fix extra költséget ad a feladathoz (pl. adminisztrációs díj).
 */
public class FixedExtraCostDecorator extends CostDecorator {
    private final double extraCost;

    public FixedExtraCostDecorator(ProjectComponent component, double extraCost) {
        super(component);
        this.extraCost = extraCost;
    }

    @Override
    public double getCalculateTotalCost() {
        return decoratedComponent.getCalculateTotalCost() + extraCost;
    }

    @Override
    public int getCompletionPercentage() {
        return decoratedComponent.getCompletionPercentage();
    }
}