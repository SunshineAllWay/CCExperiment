package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
public class Taskdef extends Typedef {
    public Taskdef() {
        setAdapterClass(TaskAdapter.class);
        setAdaptToClass(Task.class);
    }
}
