package org.apache.maven.usability;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.usability.diagnostics.DiagnosisUtils;
import org.apache.maven.usability.diagnostics.ErrorDiagnoser;
public class ArtifactNotFoundDiagnoser
    implements ErrorDiagnoser
{
    private WagonManager wagonManager;
    public boolean canDiagnose( Throwable error )
    {
        return DiagnosisUtils.containsInCausality( error, ArtifactNotFoundException.class );
    }
    public String diagnose( Throwable error )
    {
        ArtifactNotFoundException exception =
            (ArtifactNotFoundException) DiagnosisUtils.getFromCausality( error, ArtifactNotFoundException.class );
        StringBuffer message = new StringBuffer();
        message.append( "Failed to resolve artifact.\n" );
        message.append( "\nGroupId: " ).append( exception.getGroupId() );
        message.append( "\nArtifactId: " ).append( exception.getArtifactId() );
        message.append( "\nVersion: " ).append( exception.getVersion() );
        message.append( "\n\n" );
        message.append( "Reason: " ).append( exception.getMessage() );
        if ( !wagonManager.isOnline() )
        {
            message.append( "\n" ).append( SystemWarnings.getOfflineWarning() );
        }
        message.append( "\n" );
        return message.toString();
    }
}
