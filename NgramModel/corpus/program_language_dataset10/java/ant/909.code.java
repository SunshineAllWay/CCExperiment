package org.apache.tools.ant;
import org.apache.tools.ant.Task;
public class DummyTaskWithoutPublicConstructor extends Task {
    DummyTaskWithoutPublicConstructor() {
    }
    public void execute() {
    }
}
