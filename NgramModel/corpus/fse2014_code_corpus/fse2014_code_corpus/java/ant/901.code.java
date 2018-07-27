package org.apache.tools.ant;
import org.apache.tools.ant.Task;
public abstract class DummyTaskAbstract extends Task {
    public DummyTaskAbstract() {
    }
    public void execute() {
    }
    public abstract void abstractDummy();
}
