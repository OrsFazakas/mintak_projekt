package edu.bbte.protrack.logic.services;

import edu.bbte.protrack.model.entities.ProjectComponent;

/**
 * Hozzáad egy meghatározott adót az elem teljes költségéhez.
 */
public class TaxDecorator extends CostDecorator {
    private final double taxRate;

    public TaxDecorator(ProjectComponent component, double taxRate) {
        super(component);
        this.taxRate = taxRate;
    }

    @Override
    public double getCalculateTotalCost() {
        // Az eredeti költség növelése az adóval
        return decoratedComponent.getCalculateTotalCost() * (1 + taxRate);
    }

    @Override
    public int getCompletionPercentage() {
        // A haladás mértékét nem befolyásolja a költségdekorátor
        return decoratedComponent.getCompletionPercentage();
    }
}