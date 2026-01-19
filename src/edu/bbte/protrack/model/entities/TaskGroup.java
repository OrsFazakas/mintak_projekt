package edu.bbte.protrack.model.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * A Composite minta "Composite" eleme.
 * Tartalmazhat Taskokat vagy további TaskGroupokat is.
 */
public class TaskGroup extends ProjectComponent {
    private List<ProjectComponent> children;

    public TaskGroup(String name) {
        // A csoportnak önmagában nincs költsége, az az alatta lévők összege
        super(name, 0);
        this.children = new ArrayList<>();
    }

    public void addComponent(ProjectComponent component) {
        children.add(component);
    }

    public void removeComponent(ProjectComponent component) {
        children.remove(component);
    }

    /**
     * Hozzáad egy komponenst a megadott pozícióba (undo támogatáshoz).
     */
    public void addComponentAt(ProjectComponent component, int index) {
        if (index >= 0 && index <= children.size()) {
            children.add(index, component);
        } else {
            children.add(component);
        }
    }

    public List<ProjectComponent> getChildren() {
        // Visszaadjuk a listát, de érdemes lehet másolatot adni a védelem miatt
        return new ArrayList<>(children);
    }

    @Override
    public double getCalculateTotalCost() {
        // Rekurzív hívás: minden gyerek elem költségét összeadjuk
        return children.stream()
                .mapToDouble(ProjectComponent::getCalculateTotalCost)
                .sum();
    }

    @Override
    public int getCompletionPercentage() {
        if (children.isEmpty()) {
            return 0;
        }
        // Rekurzív hívás: a csoport készültsége a gyerekek átlaga
        double total = children.stream()
                .mapToDouble(ProjectComponent::getCompletionPercentage)
                .sum();
        return (int) (total / children.size());
    }

    @Override
    public String toString() {
        return String.format("📁 %s (%d elem)", name, children.size());
    }
}