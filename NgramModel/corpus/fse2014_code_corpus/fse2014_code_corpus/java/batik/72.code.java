package org.apache.batik.apps.svgbrowser;
import java.util.ArrayList;
public abstract class AbstractCompoundCommand extends AbstractUndoableCommand {
    protected ArrayList atomCommands;
    public AbstractCompoundCommand() {
        this.atomCommands = new ArrayList();
    }
    public void addCommand(UndoableCommand command) {
        if (command.shouldExecute()) {
            atomCommands.add(command);
        }
    }
    public void execute() {
        int n = atomCommands.size();
        for (int i = 0; i < n; i++) {
            UndoableCommand cmd = (UndoableCommand) atomCommands.get(i);
            cmd.execute();
        }
    }
    public void undo() {
        int size = atomCommands.size();
        for (int i = size - 1; i >= 0; i--) {
            UndoableCommand command = (UndoableCommand) atomCommands.get(i);
            command.undo();
        }
    }
    public void redo() {
        int n = atomCommands.size();
        for (int i = 0; i < n; i++) {
            UndoableCommand cmd = (UndoableCommand) atomCommands.get(i);
            cmd.redo();
        }
    }
    public boolean shouldExecute() {
        boolean shouldExecute = true;
        if (atomCommands.size() == 0) {
            shouldExecute = false;
        }
        int n = atomCommands.size();
        for (int i = 0; i < n && shouldExecute; i++) {
            UndoableCommand command = (UndoableCommand) atomCommands.get(i);
            shouldExecute = command.shouldExecute() && shouldExecute;
        }
        return shouldExecute;
    }
    public int getCommandNumber() {
        return atomCommands.size();
    }
}
