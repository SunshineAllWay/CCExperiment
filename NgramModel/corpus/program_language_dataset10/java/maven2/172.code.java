package org.apache.maven.usability;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.usability.diagnostics.ErrorDiagnoser;
public class InvalidArtifactDiagnoser
    implements ErrorDiagnoser
{
    public boolean canDiagnose( Throwable error )
    {
        return error instanceof InvalidArtifactRTException;
    }
    public String diagnose( Throwable error )
    {
        StringBuffer diagnosis = new StringBuffer();
        InvalidArtifactRTException e = (InvalidArtifactRTException) error;
        diagnosis.append( "An invalid artifact was detected.\n\n" )
            .append( "This artifact might be in your project's POM, " )
            .append( "or it might have been included transitively during the resolution process. " )
            .append( "Here is the information we do have for this artifact:\n" )
            .append( "\n    o GroupID:     " ).append( maybeFlag( e.getGroupId() ) )
            .append( "\n    o ArtifactID:  " ).append( maybeFlag( e.getArtifactId() ) )
            .append( "\n    o Version:     " ).append( maybeFlag( e.getVersion() ) )
            .append( "\n    o Type:        " ).append( maybeFlag( e.getType() ) )
            .append( "\n" );
        return diagnosis.toString();
    }
    private String maybeFlag( String value )
    {
        if ( value == null || value.trim().length() < 1 )
        {
            return "<<< MISSING >>>";
        }
        else
        {
            return value;
        }
    }
}
