package org.apache.maven.model.superpom;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = SuperPomProvider.class )
public class DefaultSuperPomProvider
    implements SuperPomProvider
{
    private Model superModel;
    @Requirement
    private ModelProcessor modelProcessor;
    public DefaultSuperPomProvider setModelProcessor( ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
        return this;
    }
    public Model getSuperModel( String version )
    {
        if ( superModel == null )
        {
            String resource = "/org/apache/maven/model/pom-" + version + ".xml";
            InputStream is = getClass().getResourceAsStream( resource );
            if ( is == null )
            {
                throw new IllegalStateException( "The super POM " + resource + " was not found"
                    + ", please verify the integrity of your Maven installation" );
            }
            try
            {
                Map<String, String> options = new HashMap<String, String>();
                options.put( "xml:4.0.0", "xml:4.0.0" );
                superModel = modelProcessor.read( is, options );
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "The super POM " + resource + " is damaged"
                    + ", please verify the integrity of your Maven installation", e );
            }
        }
        return superModel;
    }
}
