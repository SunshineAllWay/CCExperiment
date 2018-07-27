package org.apache.maven.profiles.activation;
import java.io.IOException;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
@Deprecated
public class FileProfileActivator
    extends DetectedProfileActivator
    implements LogEnabled
{
    private Logger logger;
    protected boolean canDetectActivation( Profile profile )
    {
        return profile.getActivation() != null && profile.getActivation().getFile() != null;
    }
    public boolean isActive( Profile profile )
    {
        Activation activation = profile.getActivation();
        ActivationFile actFile = activation.getFile();
        if ( actFile != null )
        {
            String fileString = actFile.getExists();
            RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
            try
            {
                interpolator.addValueSource( new EnvarBasedValueSource() );
            }
            catch ( IOException e )
            {
            }
            interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
            try
            {
                if ( StringUtils.isNotEmpty( fileString ) )
                {
                    fileString = StringUtils.replace( interpolator.interpolate( fileString, "" ), "\\", "/" );
                    return FileUtils.fileExists( fileString );
                }
                fileString = actFile.getMissing();
                if ( StringUtils.isNotEmpty( fileString ) )
                {
                    fileString = StringUtils.replace( interpolator.interpolate( fileString, "" ), "\\", "/" );
                    return !FileUtils.fileExists( fileString );
                }
            }
            catch ( InterpolationException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to interpolate missing file location for profile activator: " + fileString,
                                  e );
                }
                else
                {
                    logger.warn( "Failed to interpolate missing file location for profile activator: " + fileString
                        + ". Run in debug mode (-X) for more information." );
                }
            }
        }
        return false;
    }
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
}
