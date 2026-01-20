package edu.bbte.protrack.commands;

//A Command minta alapinterfésze.
public interface Command {
    //Végrehajtja a műveletet.
    void execute();

    //Visszavonja a műveletet (visszaállítja az előző állapotot).
    void undo();

    //Visszaadja a parancs nevét a UI-on való megjelenítéshez.
    String getName();
}