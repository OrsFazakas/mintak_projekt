package edu.bbte.protrack.model.entities;

public class Task extends ProjectComponent {
    private int completion; // 0 és 100 közötti érték
    private String assignedEmployee;

    public Task(String name, double cost, String employee) {
        super(name, cost);
        this.assignedEmployee = employee;
        this.completion = 0;
    }

    public String getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(String assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public void setCompletion(int completion) {
        if (completion < 0) this.completion = 0;
        else if (completion > 100) this.completion = 100;
        else this.completion = completion;
    }

    @Override
    public double getCalculateTotalCost() {
        // Levél esetén a költség a saját alapára
        return baseCost;
    }

    @Override
    public int getCompletionPercentage() {
        return completion;
    }
}
