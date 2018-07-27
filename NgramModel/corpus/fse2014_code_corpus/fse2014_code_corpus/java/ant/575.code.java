package org.apache.tools.ant.taskdefs.optional.vss;
import java.io.File;
import java.text.SimpleDateFormat;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.EnumeratedAttribute;
public class MSVSSHISTORY extends MSVSS {
    Commandline buildCmdLine() {
        Commandline commandLine = new Commandline();
        if (getVsspath() == null) {
            String msg = "vsspath attribute must be set!";
            throw new BuildException(msg, getLocation());
        }
        commandLine.setExecutable(getSSCommand());
        commandLine.createArgument().setValue(COMMAND_HISTORY);
        commandLine.createArgument().setValue(getVsspath());
        commandLine.createArgument().setValue(FLAG_AUTORESPONSE_DEF);  
        commandLine.createArgument().setValue(getVersionDate());
        commandLine.createArgument().setValue(getVersionLabel());
        commandLine.createArgument().setValue(getRecursive());
        commandLine.createArgument().setValue(getStyle());
        commandLine.createArgument().setValue(getLogin());
        commandLine.createArgument().setValue(getOutput());
        return commandLine;
    }
    public void setRecursive(boolean recursive) {
        super.setInternalRecursive(recursive);
    }
    public void setUser(String user) {
        super.setInternalUser(user);
    }
    public void setFromDate(String fromDate) {
        super.setInternalFromDate(fromDate);
    }
    public void setToDate(String toDate) {
        super.setInternalToDate(toDate);
    }
    public void setFromLabel(String fromLabel) {
        super.setInternalFromLabel(fromLabel);
    }
    public void setToLabel(String toLabel) {
        super.setInternalToLabel(toLabel);
    }
    public void setNumdays(int numd) {
        super.setInternalNumDays(numd);
    }
    public void setOutput(File outfile) {
        if (outfile != null) {
            super.setInternalOutputFilename(outfile.getAbsolutePath());
        }
    }
    public void setDateFormat(String dateFormat) {
        super.setInternalDateFormat(new SimpleDateFormat(dateFormat));
    }
    public void setStyle(BriefCodediffNofile attr) {
        String option = attr.getValue();
        if (option.equals(STYLE_BRIEF)) {
            super.setInternalStyle(FLAG_BRIEF);
        } else if (option.equals(STYLE_CODEDIFF)) {
            super.setInternalStyle(FLAG_CODEDIFF);
        } else if (option.equals(STYLE_DEFAULT)) {
            super.setInternalStyle("");
        } else if (option.equals(STYLE_NOFILE)) {
            super.setInternalStyle(FLAG_NO_FILE);
        } else {
            throw new BuildException("Style " + attr + " unknown.", getLocation());
        }
    }
    public static class BriefCodediffNofile extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {STYLE_BRIEF, STYLE_CODEDIFF, STYLE_NOFILE, STYLE_DEFAULT};
        }
    }
}
