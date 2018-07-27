package org.apache.maven.configuration;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
public class DefaultBeanConfiguratorTest
    extends PlexusTestCase
{
    private BeanConfigurator configurator;
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configurator = lookup( BeanConfigurator.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        configurator = null;
        super.tearDown();
    }
    private Xpp3Dom toConfig( String xml )
    {
        try
        {
            return Xpp3DomBuilder.build( new StringReader( "<configuration>" + xml + "</configuration>" ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new IllegalArgumentException( e );
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException( e );
        }
    }
    public void testMinimal()
        throws BeanConfigurationException
    {
        SomeBean bean = new SomeBean();
        Xpp3Dom config = toConfig( "<file>test</file>" );
        DefaultBeanConfigurationRequest request = new DefaultBeanConfigurationRequest();
        request.setBean( bean ).setConfiguration( config );
        configurator.configureBean( request );
        assertEquals( new File( "test" ), bean.file );
    }
    public void testPreAndPostProcessing()
        throws BeanConfigurationException
    {
        SomeBean bean = new SomeBean();
        Xpp3Dom config = toConfig( "<file>${test}</file>" );
        BeanConfigurationValuePreprocessor preprocessor = new BeanConfigurationValuePreprocessor()
        {
            public Object preprocessValue( String value, Class<?> type )
                throws BeanConfigurationException
            {
                if ( value != null && value.startsWith( "${" ) && value.endsWith( "}" ) )
                {
                    return value.substring( 2, value.length() - 1 );
                }
                return value;
            }
        };
        BeanConfigurationPathTranslator translator = new BeanConfigurationPathTranslator()
        {
            public File translatePath( File path )
            {
                return new File( "base", path.getPath() ).getAbsoluteFile();
            }
        };
        DefaultBeanConfigurationRequest request = new DefaultBeanConfigurationRequest();
        request.setBean( bean ).setConfiguration( config );
        request.setValuePreprocessor( preprocessor ).setPathTranslator( translator );
        configurator.configureBean( request );
        assertEquals( new File( "base/test" ).getAbsoluteFile(), bean.file );
    }
    public void testChildConfigurationElement()
        throws BeanConfigurationException
    {
        SomeBean bean = new SomeBean();
        Xpp3Dom config = toConfig( "<wrapper><file>test</file></wrapper>" );
        DefaultBeanConfigurationRequest request = new DefaultBeanConfigurationRequest();
        request.setBean( bean ).setConfiguration( config, "wrapper" );
        configurator.configureBean( request );
        assertEquals( new File( "test" ), bean.file );
    }
    static class SomeBean
    {
        File file;
    }
}
