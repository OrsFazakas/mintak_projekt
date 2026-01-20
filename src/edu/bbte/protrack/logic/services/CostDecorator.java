package edu.bbte.protrack.logic.services;

import edu.bbte.protrack.model.entities.ProjectComponent;


 //Absztrakt dekorátor osztály a költségek módosítására.
 // Maga is egy ProjectComponent, így beilleszthető a meglévő struktúrába.
public abstract class CostDecorator extends ProjectComponent {
    protected ProjectComponent decoratedComponent;

    public CostDecorator(ProjectComponent component) {
        super(component.getName(), component.getBaseCost());
        this.decoratedComponent = component;
    }

    @Override
    public String getName() {
        return decoratedComponent.getName();
    }
}