package org.apache.maven.model.building;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.building.ModelProblem.Severity;
public class SimpleProblemCollector
    implements ModelProblemCollector
{
    private List<String> warnings = new ArrayList<String>();
    private List<String> errors = new ArrayList<String>();
    private List<String> fatals = new ArrayList<String>();
    public List<String> getWarnings()
    {
        return warnings;
    }
    public List<String> getErrors()
    {
        return errors;
    }
    public List<String> getFatals()
    {
        return fatals;
    }
    public void add( Severity severity, String message, InputLocation location, Exception cause )
    {
        switch ( severity )
        {
            case FATAL:
                fatals.add( message );
                break;
            case ERROR:
                errors.add( message );
                break;
            case WARNING:
                warnings.add( message );
                break;
        }
    }
}
