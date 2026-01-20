package edu.bbte.protrack.model.entities;

import java.time.LocalDate;

public class Task extends ProjectComponent {
    private int completion; // 0 és 100 közötti érték
    private Employee assignedEmployee;
    private LocalDate deadline;
    private Priority priority;

    public enum Priority {
        LOW("Alacsony", "🟢"),
        MEDIUM("Közepes", "🟡"),
        HIGH("Magas", "🔴");

        private final String displayName;
        private final String icon;

        Priority(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public Task(String name, double cost, String employeeName) {
        super(name, cost);
        // Alapértelmezett Employee létrehozása a névből
        this.assignedEmployee = new Employee(employeeName);
        this.completion = 0;
        this.priority = Priority.MEDIUM;
        this.deadline = null;
    }

    public Task(String name, double cost, Employee employee) {
        super(name, cost);
        this.assignedEmployee = employee;
        this.completion = 0;
        this.priority = Priority.MEDIUM;
        this.deadline = null;
    }

    public Employee getEmployee() {
        return assignedEmployee;
    }

    public void setEmployee(Employee employee) {
        this.assignedEmployee = employee;
    }

    /**
     * Visszafelé kompatibilitás miatt - visszaadja a felelős nevét.
     */
    public String getAssignedEmployee() {
        return assignedEmployee != null ? assignedEmployee.getName() : "Nincs hozzárendelve";
    }

    /**
     * Visszafelé kompatibilitás - String alapú beállítás.
     */
    public void setAssignedEmployee(String employeeName) {
        if (this.assignedEmployee != null) {
            this.assignedEmployee.setName(employeeName);
        } else {
            this.assignedEmployee = new Employee(employeeName);
        }
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setCompletion(int completion) {
        if (completion < 0)
            this.completion = 0;
        else if (completion > 100)
            this.completion = 100;
        else
            this.completion = completion;
    }

    /**
     * Ellenőrzi, hogy a feladat késésben van-e.
     */
    public boolean isOverdue() {
        if (deadline == null || isCompleted())
            return false;
        return LocalDate.now().isAfter(deadline);
    }

    /**
     * Ellenőrzi, hogy a határidő hamarosan lejár-e (2 napon belül).
     */
    public boolean isDueSoon() {
        if (deadline == null || isCompleted())
            return false;
        LocalDate now = LocalDate.now();
        return !now.isAfter(deadline) && !now.plusDays(2).isBefore(deadline);
    }

    @Override
    public double getCalculateTotalCost() {
        return baseCost;
    }

    @Override
    public int getCompletionPercentage() {
        return completion;
    }

    @Override
    public String toString() {
        String status;
        if (isCompleted()) {
            status = "✅";
        } else if (isOverdue()) {
            status = "🚨";
        } else if (isDueSoon()) {
            status = "⚠️";
        } else {
            status = "⏳";
        }
        return String.format("%s %s %s (%d%%)", priority.getIcon(), status, name, completion);
    }

    public boolean isCompleted() {
        return completion == 100;
    }
}
