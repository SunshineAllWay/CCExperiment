package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.types.Mapper;
public class RenameExtensions extends MatchingTask {
    private String fromExtension = "";
    private String toExtension = "";
    private boolean replace = false;
    private File srcDir;
    private Mapper.MapperType globType;
    public RenameExtensions() {
        super();
        globType = new Mapper.MapperType();
        globType.setValue("glob");
    }
    public void setFromExtension(String from) {
        fromExtension = from;
    }
    public void setToExtension(String to) {
        toExtension = to;
    }
    public void setReplace(boolean replace) {
        this.replace = replace;
    }
    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }
    public void execute() throws BuildException {
        if (fromExtension == null || toExtension == null || srcDir == null) {
            throw new BuildException("srcDir, fromExtension and toExtension "
                + "attributes must be set!");
        }
        log("DEPRECATED - The renameext task is deprecated.  Use move instead.",
            Project.MSG_WARN);
        log("Replace this with:", Project.MSG_INFO);
        log("<move todir=\"" + srcDir + "\" overwrite=\"" + replace + "\">",
            Project.MSG_INFO);
        log("  <fileset dir=\"" + srcDir + "\" />", Project.MSG_INFO);
        log("  <mapper type=\"glob\"", Project.MSG_INFO);
        log("          from=\"*" + fromExtension + "\"", Project.MSG_INFO);
        log("          to=\"*" + toExtension + "\" />", Project.MSG_INFO);
        log("</move>", Project.MSG_INFO);
        log("using the same patterns on <fileset> as you\'ve used here",
            Project.MSG_INFO);
        Move move = new Move();
        move.bindToOwner(this);
        move.setOwningTarget(getOwningTarget());
        move.setTaskName(getTaskName());
        move.setLocation(getLocation());
        move.setTodir(srcDir);
        move.setOverwrite(replace);
        fileset.setDir(srcDir);
        move.addFileset(fileset);
        Mapper me = move.createMapper();
        me.setType(globType);
        me.setFrom("*" + fromExtension);
        me.setTo("*" + toExtension);
        move.execute();
    }
}
