package org.apache.maven.configuration;
import java.io.File;
public class BasedirBeanConfigurationPathTranslator
    implements BeanConfigurationPathTranslator
{
    private final File basedir;
    public BasedirBeanConfigurationPathTranslator( File basedir )
    {
        this.basedir = basedir;
    }
    public File translatePath( File path )
    {
        File result = path;
        if ( path != null && basedir != null )
        {
            if ( path.isAbsolute() )
            {
            }
            else if ( path.getPath().startsWith( File.separator ) )
            {
                result = path.getAbsoluteFile();
            }
            else
            {
                result = new File( new File( basedir, path.getPath() ).toURI().normalize() ).getAbsoluteFile();
            }
        }
        return result;
    }
}
