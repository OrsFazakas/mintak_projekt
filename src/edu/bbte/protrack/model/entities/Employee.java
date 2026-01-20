package edu.bbte.protrack.model.entities;

import java.io.Serializable;


 //Reprezentál egy munkavállalót, akihez feladatok rendelhetők.
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}