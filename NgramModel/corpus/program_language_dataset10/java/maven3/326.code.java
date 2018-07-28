package org.apache.maven.configuration.internal;
import java.io.File;
import org.apache.maven.configuration.BeanConfigurationException;
import org.apache.maven.configuration.BeanConfigurationPathTranslator;
import org.apache.maven.configuration.BeanConfigurationRequest;
import org.apache.maven.configuration.BeanConfigurationValuePreprocessor;
import org.apache.maven.configuration.BeanConfigurator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.converters.lookup.DefaultConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
@Component( role = BeanConfigurator.class )
public class DefaultBeanConfigurator
    implements BeanConfigurator
{
    private final ConverterLookup converterLookup = new DefaultConverterLookup();
    public void configureBean( BeanConfigurationRequest request )
        throws BeanConfigurationException
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "bean configuration request not specified" );
        }
        if ( request.getBean() == null )
        {
            throw new IllegalArgumentException( "bean to be configured not specified" );
        }
        Object configuration = request.getConfiguration();
        if ( configuration == null )
        {
            return;
        }
        PlexusConfiguration plexusConfig = null;
        if ( configuration instanceof PlexusConfiguration )
        {
            plexusConfig = (PlexusConfiguration) configuration;
        }
        else if ( configuration instanceof Xpp3Dom )
        {
            plexusConfig = new XmlPlexusConfiguration( (Xpp3Dom) configuration );
        }
        else
        {
            throw new BeanConfigurationException( "unsupported bean configuration source ("
                + configuration.getClass().getName() + ")" );
        }
        if ( request.getConfigurationElement() != null )
        {
            plexusConfig = plexusConfig.getChild( request.getConfigurationElement() );
        }
        ClassLoader classLoader = request.getClassLoader();
        if ( classLoader == null )
        {
            classLoader = request.getBean().getClass().getClassLoader();
        }
        BeanExpressionEvaluator evaluator = new BeanExpressionEvaluator( request );
        ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();
        try
        {
            converter.processConfiguration( converterLookup, request.getBean(), classLoader, plexusConfig, evaluator );
        }
        catch ( ComponentConfigurationException e )
        {
            throw new BeanConfigurationException( e.getMessage(), e );
        }
    }
    static class BeanExpressionEvaluator
        implements TypeAwareExpressionEvaluator
    {
        private final BeanConfigurationValuePreprocessor preprocessor;
        private final BeanConfigurationPathTranslator translator;
        public BeanExpressionEvaluator( BeanConfigurationRequest request )
        {
            preprocessor = request.getValuePreprocessor();
            translator = request.getPathTranslator();
        }
        public Object evaluate( String expression, Class<?> type )
            throws ExpressionEvaluationException
        {
            if ( preprocessor != null )
            {
                try
                {
                    return preprocessor.preprocessValue( expression, type );
                }
                catch ( BeanConfigurationException e )
                {
                    throw new ExpressionEvaluationException( e.getMessage(), e );
                }
            }
            return expression;
        }
        public Object evaluate( String expression )
            throws ExpressionEvaluationException
        {
            return evaluate( expression, null );
        }
        public File alignToBaseDirectory( File file )
        {
            if ( translator != null )
            {
                return translator.translatePath( file );
            }
            return file;
        }
    }
}
