package org.example.tasks;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class TaskdefTestSimpleTask extends Task {
    public class Echo {
        Echo() {}
        private String message = null;
        public void setMessage(String s) {message = s;}
    }
    public TaskdefTestSimpleTask() {}
    private Echo echo;
    public Echo createEcho() {
        echo = new Echo();
        return echo;
    }
    public void execute() {
        log("simpletask: "+echo.message, Project.MSG_INFO);
    }
}
