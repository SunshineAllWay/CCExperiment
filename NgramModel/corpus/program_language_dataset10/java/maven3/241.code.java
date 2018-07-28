package org.apache.maven.repository.legacy;
import org.apache.maven.wagon.providers.file.FileWagon;
public class WagonMock
    extends FileWagon
{
    private String configurableField = null;
    public void setConfigurableField( String configurableField )
    {
        this.configurableField = configurableField;
    }
    public String getConfigurableField()
    {
        return configurableField;
    }
}
