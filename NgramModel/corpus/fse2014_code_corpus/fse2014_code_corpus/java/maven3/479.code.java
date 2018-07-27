package org.apache.maven.project;
import java.util.ArrayList;
import java.util.List;
public class ExtensionDescriptor
{
    private List<String> exportedPackages;
    private List<String> exportedArtifacts;
    ExtensionDescriptor()
    {
    }
    public List<String> getExportedPackages()
    {
        if ( exportedPackages == null )
        {
            exportedPackages = new ArrayList<String>();
        }
        return exportedPackages;
    }
    public void setExportedPackages( List<String> exportedPackages )
    {
        if ( exportedPackages == null )
        {
            this.exportedPackages = null;
        }
        else
        {
            this.exportedPackages = new ArrayList<String>( exportedPackages );
        }
    }
    public List<String> getExportedArtifacts()
    {
        if ( exportedArtifacts == null )
        {
            exportedArtifacts = new ArrayList<String>();
        }
        return exportedArtifacts;
    }
    public void setExportedArtifacts( List<String> exportedArtifacts )
    {
        if ( exportedArtifacts == null )
        {
            this.exportedArtifacts = null;
        }
        else
        {
            this.exportedArtifacts = new ArrayList<String>( exportedArtifacts );
        }
    }
}
