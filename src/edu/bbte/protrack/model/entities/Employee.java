package edu.bbte.protrack.model.entities;

/**
 * Reprezentál egy munkavállalót, akihez feladatok rendelhetők.
 */
public class Employee {
    private String name;
    private String role;
    private double hourlyRate;

    public Employee(String name, String role, double hourlyRate) {
        this.name = name;
        this.role = role;
        this.hourlyRate = hourlyRate;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public double getHourlyRate() { return hourlyRate; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}