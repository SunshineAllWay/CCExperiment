package org.apache.maven.model.interpolation;
import java.util.List;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.codehaus.plexus.interpolation.ValueSource;
class ProblemDetectingValueSource
    implements ValueSource
{
    private final ValueSource valueSource;
    private final String bannedPrefix;
    private final String newPrefix;
    private final ModelProblemCollector problems;
    public ProblemDetectingValueSource( ValueSource valueSource, String bannedPrefix, String newPrefix,
                                        ModelProblemCollector problems )
    {
        this.valueSource = valueSource;
        this.bannedPrefix = bannedPrefix;
        this.newPrefix = newPrefix;
        this.problems = problems;
    }
    public Object getValue( String expression )
    {
        Object value = valueSource.getValue( expression );
        if ( value != null && expression.startsWith( bannedPrefix ) )
        {
            String msg = "The expression ${" + expression + "} is deprecated.";
            if ( newPrefix != null && newPrefix.length() > 0 )
            {
                msg += " Please use ${" + newPrefix + expression.substring( bannedPrefix.length() ) + "} instead.";
            }
            problems.add( Severity.WARNING, msg, null, null );
        }
        return value;
    }
    @SuppressWarnings( "unchecked" )
    public List getFeedback()
    {
        return valueSource.getFeedback();
    }
    public void clearFeedback()
    {
        valueSource.clearFeedback();
    }
}
