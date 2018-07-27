package org.apache.maven.project.interpolation;
import org.apache.maven.project.path.PathTranslator;
public class RegexBasedModelInterpolatorTest
    extends AbstractModelInterpolatorTest
{
    protected ModelInterpolator createInterpolator( PathTranslator translator )
        throws Exception
    {
        RegexBasedModelInterpolator interpolator = new RegexBasedModelInterpolator( translator );
        interpolator.initialize();
        return interpolator;
    }
    protected ModelInterpolator createInterpolator()
        throws Exception
    {
        RegexBasedModelInterpolator interpolator = new RegexBasedModelInterpolator();
        interpolator.initialize();
        return interpolator;
    }
}
