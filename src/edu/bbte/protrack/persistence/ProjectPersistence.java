package edu.bbte.protrack.persistence;

import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.entities.ProjectComponent;

import java.io.*;

//Egyszerű fájl-alapú perzisztencia Java szerializációval.
//A projekt adatokat .dat fájlban tárolja.
public class ProjectPersistence {
    private static final String DEFAULT_FILE_PATH = "protrack_data.dat";
    private final String filePath;

    public ProjectPersistence() {
        this.filePath = DEFAULT_FILE_PATH;
    }

    public ProjectPersistence(String filePath) {
        this.filePath = filePath;
    }


    //Elmenti a projekt gyökérelemet fájlba.
    public boolean save(TaskGroup rootProject) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            oos.writeObject(rootProject);
            System.out.println("Projekt sikeresen mentve: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Hiba a projekt mentésekor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Betölti a projektet a fájlból.
    public TaskGroup load() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Nincs mentett projekt: " + filePath);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {
            TaskGroup loaded = (TaskGroup) ois.readObject();
            System.out.println("Projekt sikeresen betöltve: " + filePath);
            return loaded;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Hiba a projekt betöltésekor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //Ellenőrzi, hogy létezik-e mentett projekt.
    public boolean hasSavedData() {
        return new File(filePath).exists();
    }


    //Törli a mentett adatokat.
    public boolean deleteSavedData() {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public String getFilePath() {
        return filePath;
    }
}
