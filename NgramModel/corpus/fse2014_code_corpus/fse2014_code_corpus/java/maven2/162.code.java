package org.apache.maven.plugin;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.path.PathTranslator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
public class PluginParameterExpressionEvaluator
    implements ExpressionEvaluator
{
    private static final Map BANNED_EXPRESSIONS;
    private static final Map DEPRECATED_EXPRESSIONS;
    static
    {
        Map deprecated = new HashMap();
        deprecated.put( "project.build.resources", "project.resources" );
        deprecated.put( "project.build.testResources", "project.testResources" );
        DEPRECATED_EXPRESSIONS = deprecated;
        Map banned = new HashMap();
        BANNED_EXPRESSIONS = banned;
    }
    private final PathTranslator pathTranslator;
    private final MavenSession context;
    private final Logger logger;
    private final MojoExecution mojoExecution;
    private final MavenProject project;
    private final String basedir;
    private final Properties properties;
    public PluginParameterExpressionEvaluator( MavenSession context,
                                               MojoExecution mojoExecution,
                                               PathTranslator pathTranslator,
                                               Logger logger,
                                               MavenProject project,
                                               Properties properties )
    {
        this.context = context;
        this.mojoExecution = mojoExecution;
        this.pathTranslator = pathTranslator;
        this.logger = logger;
        this.project = project;
        this.properties = properties;
        String basedir = null;
        if ( project != null )
        {
            File baseDir = project.getBasedir();
            if ( baseDir != null )
            {
                basedir = baseDir.getAbsolutePath();
            }
        }
        if ( basedir == null )
        {
            basedir = System.getProperty( "user.dir" );
        }
        this.basedir = basedir;
    }
    public Object evaluate( String expr )
        throws ExpressionEvaluationException
    {
        Object value = null;
        if ( expr == null )
        {
            return null;
        }
        String expression = stripTokens( expr );
        if ( expression.equals( expr ) )
        {
            int index = expr.indexOf( "${" );
            if ( index >= 0 )
            {
                int lastIndex = expr.indexOf( "}", index );
                if ( lastIndex >= 0 )
                {
                    String retVal = expr.substring( 0, index );
                    if ( index > 0 && expr.charAt( index - 1 ) == '$' )
                    {
                        retVal += expr.substring( index + 1, lastIndex + 1 );
                    }
                    else
                    {
                        retVal += evaluate( expr.substring( index, lastIndex + 1 ) );
                    }
                    retVal += evaluate( expr.substring( lastIndex + 1 ) );
                    return retVal;
                }
            }
            if ( expression.indexOf( "$$" ) > -1 )
            {
                return expression.replaceAll( "\\$\\$", "\\$" );
            }
            else
            {
                return expression;
            }
        }
        MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
        if ( BANNED_EXPRESSIONS.containsKey( expression ) )
        {
            throw new ExpressionEvaluationException( "The parameter expression: \'" + expression
                + "\' used in mojo: \'" + mojoDescriptor.getGoal() + "\' is banned. Use \'"
                + BANNED_EXPRESSIONS.get( expression ) + "\' instead." );
        }
        else if ( DEPRECATED_EXPRESSIONS.containsKey( expression ) )
        {
            logger.warn( "The parameter expression: \'" + expression + "\' used in mojo: \'"
                + mojoDescriptor.getGoal() + "\' has been deprecated. Use \'" + DEPRECATED_EXPRESSIONS.get( expression )
                + "\' instead." );
        }
        if ( "localRepository".equals( expression ) )
        {
            value = context.getLocalRepository();
        }
        else if ( "session".equals( expression ) )
        {
            value = context;
        }
        else if ( "reactorProjects".equals( expression ) )
        {
            value = context.getSortedProjects();
        }
        else if ( "reports".equals( expression ) )
        {
            value = mojoExecution.getReports();
        }
        else if ( "mojoExecution".equals( expression ) )
        {
        	value = mojoExecution;
        }
        else if ( "project".equals( expression ) )
        {
            value = project;
        }
        else if ( "executedProject".equals( expression ) )
        {
            value = project.getExecutionProject();
        }
        else if ( expression.startsWith( "project" ) )
        {
            try
            {
                int pathSeparator = expression.indexOf( "/" );
                if ( pathSeparator > 0 )
                {
                    String pathExpression = expression.substring( 0, pathSeparator );
                    value = ReflectionValueExtractor.evaluate( pathExpression, project );
                    value = value + expression.substring( pathSeparator );
                }
                else
                {
                    value = ReflectionValueExtractor.evaluate( expression.substring( 1 ), project );
                }
            }
            catch ( Exception e )
            {
                throw new ExpressionEvaluationException( "Error evaluating plugin parameter expression: " + expression,
                                                         e );
            }
        }
        else if ( expression.startsWith( "plugin" ) )
        {
            try
            {
                int pathSeparator = expression.indexOf( "/" );
                PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();
                if ( pathSeparator > 0 )
                {
                    String pathExpression = expression.substring( 1, pathSeparator );
                    value = ReflectionValueExtractor.evaluate( pathExpression, pluginDescriptor );
                    value = value + expression.substring( pathSeparator );
                }
                else
                {
                    value = ReflectionValueExtractor.evaluate( expression.substring( 1 ), pluginDescriptor );
                }
            }
            catch ( Exception e )
            {
                throw new ExpressionEvaluationException( "Error evaluating plugin parameter expression: " + expression,
                                                         e );
            }
        }
        else if ( "settings".equals( expression ) )
        {
            value = context.getSettings();
        }
        else if ( expression.startsWith( "settings" ) )
        {
            try
            {
                int pathSeparator = expression.indexOf( "/" );
                if ( pathSeparator > 0 )
                {
                    String pathExpression = expression.substring( 1, pathSeparator );
                    value = ReflectionValueExtractor.evaluate( pathExpression, context.getSettings() );
                    value = value + expression.substring( pathSeparator );
                }
                else
                {
                    value = ReflectionValueExtractor.evaluate( expression.substring( 1 ), context.getSettings() );
                }
            }
            catch ( Exception e )
            {
                throw new ExpressionEvaluationException( "Error evaluating plugin parameter expression: " + expression,
                                                         e );
            }
        }
        else if ( "basedir".equals( expression ) )
        {
            value = basedir;
        }
        else if ( expression.startsWith( "basedir" ) )
        {
            int pathSeparator = expression.indexOf( "/" );
            if ( pathSeparator > 0 )
            {
                value = basedir + expression.substring( pathSeparator );
            }
            else
            {
                logger.error( "Got expression '" + expression + "' that was not recognised" );
            }
        }
        if ( value == null )
        {
            if ( value == null && properties != null )
            {
                value = properties.getProperty( expression );
            }
            if ( value == null && project != null && project.getProperties() != null )
            {
                value = project.getProperties().getProperty( expression );
            }
        }
        if ( value instanceof String )
        {
            String val = (String) value;
            int exprStartDelimiter = val.indexOf( "${" );
            if ( exprStartDelimiter >= 0 )
            {
                if ( exprStartDelimiter > 0 )
                {
                    value = val.substring( 0, exprStartDelimiter ) + evaluate( val.substring( exprStartDelimiter ) );
                }
                else
                {
                    value = evaluate( val.substring( exprStartDelimiter ) );
                }
            }
        }
        return value;
    }
    private String stripTokens( String expr )
    {
        if ( expr.startsWith( "${" ) && expr.indexOf( "}" ) == expr.length() - 1 )
        {
            expr = expr.substring( 2, expr.length() - 1 );
        }
        return expr;
    }
    public File alignToBaseDirectory( File file )
    {
        File basedir;
        if ( project != null && project.getFile() != null )
        {
            basedir = project.getFile().getParentFile();
        }
        else
        {
            basedir = new File( "." ).getAbsoluteFile().getParentFile();
        }
        return new File( pathTranslator.alignToBaseDirectory( file.getPath(), basedir ) );
    }
}
