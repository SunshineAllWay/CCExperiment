package org.apache.maven.plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.util.ArrayList;
import java.util.List;
public class MojoExecution
{
    public static final String DEFAULT_EXEC_ID_PREFIX = "default-";
    public static final String CLI_EXECUTION_ID = DEFAULT_EXEC_ID_PREFIX + "cli";
    private final String executionId;
    private final MojoDescriptor mojoDescriptor;
    private Xpp3Dom configuration;
    private List forkedExecutions = new ArrayList();
    private List reports;
    public MojoExecution( MojoDescriptor mojoDescriptor )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.configuration = null;
        this.executionId = DEFAULT_EXEC_ID_PREFIX + mojoDescriptor.getGoal();
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, String executionId )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.executionId = executionId;
        this.configuration = null;
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, Xpp3Dom configuration )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.configuration = configuration;
        this.executionId = DEFAULT_EXEC_ID_PREFIX + mojoDescriptor.getGoal();
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, Xpp3Dom configuration, String executionId )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.configuration = configuration;
        this.executionId = executionId;
    }
    public String getExecutionId()
    {
        return executionId;
    }
    public MojoDescriptor getMojoDescriptor()
    {
        return mojoDescriptor;
    }
    public Xpp3Dom getConfiguration()
    {
        return configuration;
    }
    public void addMojoExecution( MojoExecution execution )
    {
        forkedExecutions.add( execution );
    }
    public void setReports( List reports )
    {
        this.reports = reports;
    }
    public List getReports()
    {
        return reports;
    }
    public List getForkedExecutions()
    {
        return forkedExecutions;
    }
    public void setConfiguration( Xpp3Dom configuration )
    {
        this.configuration = configuration;
    }
}
