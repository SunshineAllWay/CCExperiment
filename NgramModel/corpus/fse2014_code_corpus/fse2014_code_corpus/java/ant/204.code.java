package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class Filter extends Task {
    private String token;
    private String value;
    private File filtersFile;
    public void setToken(String token) {
        this.token = token;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setFiltersfile(File filtersFile) {
        this.filtersFile = filtersFile;
    }
    public void execute() throws BuildException {
        boolean isFiltersFromFile =
            filtersFile != null && token == null && value == null;
        boolean isSingleFilter =
            filtersFile == null && token != null && value != null;
        if (!isFiltersFromFile && !isSingleFilter) {
            throw new BuildException("both token and value parameters, or "
                                     + "only a filtersFile parameter is "
                                     + "required", getLocation());
        }
        if (isSingleFilter) {
            getProject().getGlobalFilterSet().addFilter(token, value);
        }
        if (isFiltersFromFile) {
            readFilters();
        }
    }
    protected void readFilters() throws BuildException {
        log("Reading filters from " + filtersFile, Project.MSG_VERBOSE);
        getProject().getGlobalFilterSet().readFiltersFromFile(filtersFile);
    }
}
