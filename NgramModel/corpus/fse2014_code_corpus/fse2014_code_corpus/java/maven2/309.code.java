package org.apache.maven.project.interpolation;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.project.path.PathTranslator;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
public class RegexBasedModelInterpolator
    extends AbstractStringBasedModelInterpolator
{
    public RegexBasedModelInterpolator()
        throws IOException
    {
    }
    public RegexBasedModelInterpolator( PathTranslator pathTranslator )
    {
        super( pathTranslator );
    }
    public RegexBasedModelInterpolator( Properties envars )
    {
    }
    protected Interpolator createInterpolator()
    {
        return new RegexBasedInterpolator( true );
    }
}
