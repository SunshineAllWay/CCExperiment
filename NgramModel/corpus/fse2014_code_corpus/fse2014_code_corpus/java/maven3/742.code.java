package org.apache.maven.model.locator;
import java.io.File;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ModelLocator.class )
public class DefaultModelLocator
    implements ModelLocator
{
    public File locatePom( File projectDirectory )
    {
        return new File( projectDirectory, "pom.xml" );
    }
}
