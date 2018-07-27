package org.apache.maven.project.interpolation;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.path.PathTranslator;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PrefixedValueSourceWrapper;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.ValueSource;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
@Deprecated
public abstract class AbstractStringBasedModelInterpolator
    extends AbstractLogEnabled
    implements ModelInterpolator, Initializable
{
    private static final List<String> PROJECT_PREFIXES = Arrays.asList( new String[]{ "pom.", "project." } );
    private static final List<String> TRANSLATED_PATH_EXPRESSIONS;
    static
    {
        List<String> translatedPrefixes = new ArrayList<String>();
        translatedPrefixes.add( "build.directory" );
        translatedPrefixes.add( "build.outputDirectory" );
        translatedPrefixes.add( "build.testOutputDirectory" );
        translatedPrefixes.add( "build.sourceDirectory" );
        translatedPrefixes.add( "build.testSourceDirectory" );
        translatedPrefixes.add( "build.scriptSourceDirectory" );
        translatedPrefixes.add( "reporting.outputDirectory" );
        TRANSLATED_PATH_EXPRESSIONS = translatedPrefixes;
    }
    private PathTranslator pathTranslator;
    private Interpolator interpolator;
    private RecursionInterceptor recursionInterceptor;
    protected AbstractStringBasedModelInterpolator( PathTranslator pathTranslator )
    {
        this.pathTranslator = pathTranslator;
    }
    protected AbstractStringBasedModelInterpolator()
    {
    }
    public Model interpolate( Model model, Map<String, ?> context )
        throws ModelInterpolationException
    {
        return interpolate( model, context, true );
    }
    public Model interpolate( Model model, Map<String, ?> context, boolean strict )
        throws ModelInterpolationException
    {
        Properties props = new Properties();
        props.putAll( context );
        return interpolate( model,
                            null,
                            new DefaultProjectBuilderConfiguration().setExecutionProperties( props ),
                            true );
    }
    public Model interpolate( Model model,
                              File projectDir,
                              ProjectBuilderConfiguration config,
                              boolean debugEnabled )
        throws ModelInterpolationException
    {
        StringWriter sWriter = new StringWriter( 1024 );
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try
        {
            writer.write( sWriter, model );
        }
        catch ( IOException e )
        {
            throw new ModelInterpolationException( "Cannot serialize project model for interpolation.", e );
        }
        String serializedModel = sWriter.toString();
        serializedModel = interpolate( serializedModel, model, projectDir, config, debugEnabled );
        StringReader sReader = new StringReader( serializedModel );
        MavenXpp3Reader modelReader = new MavenXpp3Reader();
        try
        {
            model = modelReader.read( sReader );
        }
        catch ( IOException e )
        {
            throw new ModelInterpolationException(
                "Cannot read project model from interpolating filter of serialized version.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ModelInterpolationException(
                "Cannot read project model from interpolating filter of serialized version.", e );
        }
        return model;
    }
    public String interpolate( String src,
                               Model model,
                               final File projectDir,
                               ProjectBuilderConfiguration config,
                               boolean debug )
        throws ModelInterpolationException
    {
        try
        {
            List<ValueSource> valueSources = createValueSources( model, projectDir, config );
            List<InterpolationPostProcessor> postProcessors = createPostProcessors( model, projectDir, config );
            return interpolateInternal( src, valueSources, postProcessors, debug );
        }
        finally
        {
            interpolator.clearAnswers();
        }
    }
    protected List<ValueSource> createValueSources( final Model model, final File projectDir,
                                                    final ProjectBuilderConfiguration config )
    {
        String timestampFormat = DEFAULT_BUILD_TIMESTAMP_FORMAT;
        Properties modelProperties = model.getProperties();
        if ( modelProperties != null )
        {
            timestampFormat = modelProperties.getProperty( BUILD_TIMESTAMP_FORMAT_PROPERTY, timestampFormat );
        }
        ValueSource modelValueSource1 = new PrefixedObjectValueSource( PROJECT_PREFIXES, model, false );
        ValueSource modelValueSource2 = new ObjectBasedValueSource( model );
        ValueSource basedirValueSource = new PrefixedValueSourceWrapper( new AbstractValueSource( false )
        {
            public Object getValue( String expression )
            {
                if ( projectDir != null && "basedir".equals( expression ) )
                {
                    return projectDir.getAbsolutePath();
                }
                return null;
            }
        }, PROJECT_PREFIXES, true );
        ValueSource baseUriValueSource = new PrefixedValueSourceWrapper( new AbstractValueSource( false )
        {
            public Object getValue( String expression )
            {
                if ( projectDir != null && "baseUri".equals( expression ) )
                {
                    return projectDir.getAbsoluteFile().toURI().toString();
                }
                return null;
            }
        }, PROJECT_PREFIXES, false );
        List<ValueSource> valueSources = new ArrayList<ValueSource>( 9 );
        valueSources.add( basedirValueSource );
        valueSources.add( baseUriValueSource );
        valueSources.add( new BuildTimestampValueSource( config.getBuildStartTime(), timestampFormat ) );
        valueSources.add( modelValueSource1 );
        valueSources.add( new MapBasedValueSource( config.getUserProperties() ) );
        valueSources.add( new MapBasedValueSource( modelProperties ) );
        valueSources.add( new MapBasedValueSource( config.getExecutionProperties() ) );
        valueSources.add( new AbstractValueSource( false )
        {
            public Object getValue( String expression )
            {
                return config.getExecutionProperties().getProperty( "env." + expression );
            }
        } );
        valueSources.add( modelValueSource2 );
        return valueSources;
    }
    protected List<InterpolationPostProcessor> createPostProcessors( final Model model, final File projectDir,
                                                                     final ProjectBuilderConfiguration config )
    {
        return Collections.singletonList( (InterpolationPostProcessor) new PathTranslatingPostProcessor(
                                                                                                         PROJECT_PREFIXES,
                                                                                                         TRANSLATED_PATH_EXPRESSIONS,
                                                                                                         projectDir,
                                                                                                         pathTranslator ) );
    }
    @SuppressWarnings("unchecked")
    protected String interpolateInternal( String src, List<ValueSource> valueSources,
                                          List<InterpolationPostProcessor> postProcessors, boolean debug )
        throws ModelInterpolationException
    {
        if ( src.indexOf( "${" ) < 0 )
        {
            return src;
        }
        Logger logger = getLogger();
        String result = src;
        synchronized( this )
        {
            for ( ValueSource vs : valueSources )
            {
                interpolator.addValueSource( vs );
            }
            for ( InterpolationPostProcessor postProcessor : postProcessors )
            {
                interpolator.addPostProcessor( postProcessor );
            }
            try
            {
                try
                {
                    result = interpolator.interpolate( result, recursionInterceptor );
                }
                catch( InterpolationException e )
                {
                    throw new ModelInterpolationException( e.getMessage(), e );
                }
                if ( debug )
                {
                    List<Object> feedback = interpolator.getFeedback();
                    if ( feedback != null && !feedback.isEmpty() )
                    {
                        logger.debug( "Maven encountered the following problems during initial POM interpolation:" );
                        Object last = null;
                        for ( Object next : feedback )
                        {
                            if ( next instanceof Throwable )
                            {
                                if ( last == null )
                                {
                                    logger.debug( "", ( (Throwable) next ) );
                                }
                                else
                                {
                                    logger.debug( String.valueOf( last ), ( (Throwable) next ) );
                                }
                            }
                            else
                            {
                                if ( last != null )
                                {
                                    logger.debug( String.valueOf( last ) );
                                }
                                last = next;
                            }
                        }
                        if ( last != null )
                        {
                            logger.debug( String.valueOf( last ) );
                        }
                    }
                }
                interpolator.clearFeedback();
            }
            finally
            {
                for ( ValueSource vs : valueSources )
                {
                    interpolator.removeValuesSource( vs );
                }
                for ( InterpolationPostProcessor postProcessor : postProcessors )
                {
                    interpolator.removePostProcessor( postProcessor );
                }
            }
        }
        return result;
    }
    protected RecursionInterceptor getRecursionInterceptor()
    {
        return recursionInterceptor;
    }
    protected void setRecursionInterceptor( RecursionInterceptor recursionInterceptor )
    {
        this.recursionInterceptor = recursionInterceptor;
    }
    protected abstract Interpolator createInterpolator();
    public void initialize()
        throws InitializationException
    {
        interpolator = createInterpolator();
        recursionInterceptor = new PrefixAwareRecursionInterceptor( PROJECT_PREFIXES );
    }
    protected final Interpolator getInterpolator()
    {
        return interpolator;
    }
}
