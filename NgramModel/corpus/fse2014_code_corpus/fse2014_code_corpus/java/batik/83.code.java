package org.apache.batik.apps.svgbrowser;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
public class HistoryBrowser {
    public static final int EXECUTING = 1;
    public static final int UNDOING = 2;
    public static final int REDOING = 3;
    public static final int IDLE = 4;
    protected EventListenerList eventListeners =
        new EventListenerList();
    protected ArrayList history;
    protected int currentCommandIndex = -1;
    protected int historySize = 1000;
    protected int state = IDLE;
    protected CommandController commandController;
    public HistoryBrowser(CommandController commandController) {
        this.history = new ArrayList();
        this.commandController = commandController;
    }
    public HistoryBrowser(int historySize) {
        this.history = new ArrayList();
        setHistorySize(historySize);
    }
    protected void setHistorySize(int size) {
        historySize = size;
    }
    public void setCommandController(CommandController newCommandController) {
        this.commandController = newCommandController;
    }
    public void addCommand(UndoableCommand command) {
        int n = history.size();
        for (int i = n - 1; i > currentCommandIndex; i--) {
            history.remove(i);
        }
        if (commandController != null) {
            commandController.execute(command);
        } else {
            state = EXECUTING;
            command.execute();
            state = IDLE;
        }
        history.add(command);
        currentCommandIndex = history.size() - 1;
        if (currentCommandIndex >= historySize) {
            history.remove(0);
            currentCommandIndex--;
        }
        fireExecutePerformed(new HistoryBrowserEvent(new CommandNamesInfo(
                command.getName(), getLastUndoableCommandName(),
                getLastRedoableCommandName())));
    }
    public void undo() {
        if (history.isEmpty() || currentCommandIndex < 0) {
            return;
        }
        UndoableCommand command = (UndoableCommand) history
                .get(currentCommandIndex);
        if (commandController != null) {
            commandController.undo(command);
        } else {
            state = UNDOING;
            command.undo();
            state = IDLE;
        }
        currentCommandIndex--;
        fireUndoPerformed(new HistoryBrowserEvent(new CommandNamesInfo(command
                .getName(), getLastUndoableCommandName(),
                getLastRedoableCommandName())));
    }
    public void redo() {
        if (history.isEmpty() || currentCommandIndex == history.size() - 1) {
            return;
        }
        UndoableCommand command = (UndoableCommand) history
                .get(++currentCommandIndex);
        if (commandController != null) {
            commandController.redo(command);
        } else {
            state = REDOING;
            command.redo();
            state = IDLE;
        }
        fireRedoPerformed(new HistoryBrowserEvent(new CommandNamesInfo(command
                .getName(), getLastUndoableCommandName(),
                getLastRedoableCommandName())));
    }
    public void compoundUndo(int undoNumber) {
        for (int i = 0; i < undoNumber; i++) {
            undo();
        }
    }
    public void compoundRedo(int redoNumber) {
        for (int i = 0; i < redoNumber; i++) {
            redo();
        }
    }
    public String getLastUndoableCommandName() {
        if (history.isEmpty() || currentCommandIndex < 0) {
            return "";
        }
        return ((UndoableCommand) history.get(currentCommandIndex)).getName();
    }
    public String getLastRedoableCommandName() {
        if (history.isEmpty() || currentCommandIndex == history.size() - 1) {
            return "";
        }
        return ((UndoableCommand) history.get(currentCommandIndex + 1))
                .getName();
    }
    public void resetHistory() {
        history.clear();
        currentCommandIndex = -1;
        fireHistoryReset(new HistoryBrowserEvent(new Object()));
    }
    public int getState() {
        if (commandController != null) {
            return commandController.getState();
        } else {
            return state;
        }
    }
    public static class HistoryBrowserEvent extends EventObject {
        public HistoryBrowserEvent(Object source) {
            super(source);
        }
    }
    public static interface HistoryBrowserListener extends EventListener {
        void executePerformed(HistoryBrowserEvent event);
        void undoPerformed(HistoryBrowserEvent event);
        void redoPerformed(HistoryBrowserEvent event);
        void historyReset(HistoryBrowserEvent event);
        void doCompoundEdit(HistoryBrowserEvent event);
        void compoundEditPerformed(HistoryBrowserEvent event);
    }
    public static class HistoryBrowserAdapter implements HistoryBrowserListener {
        public void executePerformed(HistoryBrowserEvent event) {
        }
        public void undoPerformed(HistoryBrowserEvent event) {
        }
        public void redoPerformed(HistoryBrowserEvent event) {
        }
        public void historyReset(HistoryBrowserEvent event) {
        }
        public void compoundEditPerformed(HistoryBrowserEvent event) {
        }
        public void doCompoundEdit(HistoryBrowserEvent event) {
        }
    }
    public void addListener(HistoryBrowserListener listener) {
        eventListeners.add(HistoryBrowserListener.class, listener);
    }
    public void fireExecutePerformed(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .executePerformed(event);
            }
        }
    }
    public void fireUndoPerformed(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .undoPerformed(event);
            }
        }
    }
    public void fireRedoPerformed(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .redoPerformed(event);
            }
        }
    }
    public void fireHistoryReset(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .historyReset(event);
            }
        }
    }
    public void fireDoCompoundEdit(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .doCompoundEdit(event);
            }
        }
    }
    public void fireCompoundEditPerformed(HistoryBrowserEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == HistoryBrowserListener.class) {
                ((HistoryBrowserListener) listeners[i + 1])
                        .compoundEditPerformed(event);
            }
        }
    }
    public static class CommandNamesInfo {
        private String lastUndoableCommandName;
        private String lastRedoableCommandName;
        private String commandName;
        public CommandNamesInfo(String commandName,
                                String lastUndoableCommandName,
                                String lastRedoableCommandName) {
            this.lastUndoableCommandName = lastUndoableCommandName;
            this.lastRedoableCommandName = lastRedoableCommandName;
            this.commandName = commandName;
        }
        public String getLastRedoableCommandName() {
            return lastRedoableCommandName;
        }
        public String getLastUndoableCommandName() {
            return lastUndoableCommandName;
        }
        public String getCommandName() {
            return commandName;
        }
    }
    public static interface CommandController {
        void execute(UndoableCommand command);
        void undo(UndoableCommand command);
        void redo(UndoableCommand command);
        int getState();
    }
    public static class DocumentCommandController implements CommandController {
        protected DOMViewerController controller;
        protected int state = HistoryBrowser.IDLE;
        public DocumentCommandController(DOMViewerController controller) {
            this.controller = controller;
        }
        public void execute(final UndoableCommand command) {
            Runnable r = new Runnable() {
                public void run() {
                    state = HistoryBrowser.EXECUTING;
                    command.execute();
                    state = HistoryBrowser.IDLE;
                }
            };
            controller.performUpdate(r);
        }
        public void undo(final UndoableCommand command) {
            Runnable r = new Runnable() {
                public void run() {
                    state = HistoryBrowser.UNDOING;
                    command.undo();
                    state = HistoryBrowser.IDLE;
                }
            };
            controller.performUpdate(r);
        }
        public void redo(final UndoableCommand command) {
            Runnable r = new Runnable() {
                public void run() {
                    state = HistoryBrowser.REDOING;
                    command.redo();
                    state = HistoryBrowser.IDLE;
                }
            };
            controller.performUpdate(r);
        }
        public int getState() {
            return state;
        }
    }
}
