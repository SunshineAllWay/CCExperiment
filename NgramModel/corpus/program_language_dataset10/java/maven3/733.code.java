package org.apache.maven.model.interpolation;
import org.apache.maven.model.path.PathTranslator;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.util.ValueSourceUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
class PathTranslatingPostProcessor
    implements InterpolationPostProcessor
{
    private final Collection<String> unprefixedPathKeys;
    private final File projectDir;
    private final PathTranslator pathTranslator;
    private final List<String> expressionPrefixes;
    public PathTranslatingPostProcessor( List<String> expressionPrefixes, Collection<String> unprefixedPathKeys,
                                         File projectDir, PathTranslator pathTranslator )
    {
        this.expressionPrefixes = expressionPrefixes;
        this.unprefixedPathKeys = unprefixedPathKeys;
        this.projectDir = projectDir;
        this.pathTranslator = pathTranslator;
    }
    public Object execute( String expression, Object value )
    {
        if ( value != null )
        {
            expression = ValueSourceUtils.trimPrefix( expression, expressionPrefixes, true );
            if ( unprefixedPathKeys.contains( expression ) )
            {
                return pathTranslator.alignToBaseDirectory( String.valueOf( value ), projectDir );
            }
        }
        return null;
    }
}
