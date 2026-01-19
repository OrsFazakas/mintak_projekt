package edu.bbte.protrack.model.entities;

import java.io.Serializable;

public abstract class ProjectComponent implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected double baseCost;

    public ProjectComponent(String name, double baseCost) {
        this.name = name;
        this.baseCost = baseCost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaseCost() {
        return baseCost;
    }

    // Absztrakt metódusok, amiket a leszármazottak specifikusan számolnak ki
    public abstract double getCalculateTotalCost();
    public abstract int getCompletionPercentage();
}
