package org.apache.maven.usability;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginParameterException;
import org.apache.maven.usability.diagnostics.DiagnosisUtils;
import org.apache.maven.usability.diagnostics.ErrorDiagnoser;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
public class PluginConfigurationDiagnoser
    implements ErrorDiagnoser
{
    public boolean canDiagnose( Throwable error )
    {
        return DiagnosisUtils.containsInCausality( error, PluginConfigurationException.class );
    }
    public String diagnose( Throwable error )
    {
        PluginConfigurationException pce =
            (PluginConfigurationException) DiagnosisUtils.getFromCausality( error, PluginConfigurationException.class );
        if ( pce instanceof PluginParameterException )
        {
            PluginParameterException exception = (PluginParameterException) pce;
            return exception.buildDiagnosticMessage();
        }
        else if ( DiagnosisUtils.containsInCausality( pce, ComponentConfigurationException.class ) )
        {
            ComponentConfigurationException cce = (ComponentConfigurationException) DiagnosisUtils.getFromCausality(
                pce, ComponentConfigurationException.class );
            return pce.buildConfigurationDiagnosticMessage( cce );
        }
        else
        {
            return pce.getMessage();
        }
    }
}
